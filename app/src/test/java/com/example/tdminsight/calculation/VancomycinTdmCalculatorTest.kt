package com.example.tdminsight.calculation

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.model.TdmResultValue
import com.example.tdminsight.model.WorkflowType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VancomycinTdmCalculatorTest {

    private val calculator = VancomycinTdmCalculator()

    @Test
    fun preCalculationMatchesApprovedEquation() {
        val result = calculator.calculate(preInput())

        assertValue(result, "Adult Vd estimate", 38.9, 1e-12, "L")
        assertValue(result, "Cmax", 40.7069408740, 1e-9, "mg/L")
        assertValue(result, "Ke", 0.0831957012, 1e-10, "h⁻¹")
        assertValue(result, "Half-life", 8.3297573085, 1e-9, "h")
        assertValue(result, "Cmin", 15.0, 1e-12, "mg/L")
        assertFalse(result.finalValues.any { it.label == "AUC24" })

        assertExplanationTitles(
            result,
            "Adult Volume of Distribution",
            "Estimated Peak Concentration",
            "Elimination Rate Constant",
            "Half-life"
        )
    }

    @Test
    fun postCalculationUsesPopulationKe() {
        val result = calculator.calculate(postInput())

        assertValue(result, "Adult Vd estimate", 38.9, 1e-12, "L")
        assertValue(result, "Ke", 0.0708, 1e-12, "h⁻¹")
        assertValue(result, "Cmax", 27.8011065780, 1e-9, "mg/L")
        assertValue(result, "Cmin", 11.8873620701, 1e-9, "mg/L")
        assertValue(result, "Half-life", 9.7881355932, 1e-9, "h")
        assertFalse(result.finalValues.any { it.label == "AUC24" })

        assertExplanationTitles(
            result,
            "Adult Volume of Distribution",
            "Population Elimination Rate Constant",
            "Extrapolated Peak Concentration",
            "Projected Trough Concentration",
            "Half-life"
        )
    }

    @Test
    fun prePostWorkedCaseMatchesMohReference() {
        val result = calculator.calculate(prePostInput())

        // Source-reported MOH worked-case values are intentionally asserted at source precision.
        assertValue(result, "Ke", 0.0643, 0.001, "h⁻¹")
        assertValue(result, "Half-life", 10.78, 0.1, "h")
        assertValue(result, "Cmax", 31.25, 0.1, "mg/L")
        assertValue(result, "Cmin", 14.44, 0.1, "mg/L")
        assertValue(result, "Co", 35.53, 0.1, "mg/L")
        assertValue(result, "AUC interval", 328.0, 1.0, "mg·h/L")
        assertValue(result, "AUC24", 656.0, 1.0, "mg·h/L")
    }

    @Test
    fun prePostVolumeOfDistributionUsesApprovedEquation() {
        val result = calculator.calculate(prePostInput())

        assertValue(result, "Vd", 44.6159537758, 1e-9, "L")
        assertValue(result, "Vd/BW", 0.6373707682, 1e-10, "L/kg")
    }

    @Test
    fun prePostAucStructureMatchesApprovedFormula() {
        val result = calculator.calculate(prePostInput())
        val aucInterval = result.value("AUC interval")
        val dosingFrequency = result.value("Dosing frequency per day")
        val auc24 = result.value("AUC24")

        assertEquals(327.9477207038, aucInterval.value, 1e-9)
        assertEquals(2.0, dosingFrequency.value, 1e-12)
        assertEquals(aucInterval.value * 2.0, auc24.value, 1e-9)
        assertEquals("mg·h/L", aucInterval.unit)
        assertEquals("mg·h/L", auc24.unit)
    }

    @Test
    fun resultUnitsMatchApprovedSpecification() {
        val prePost = calculator.calculate(prePostInput())

        assertEquals("h⁻¹", prePost.value("Ke").unit)
        assertEquals("h", prePost.value("Half-life").unit)
        assertEquals("L", prePost.value("Vd").unit)
        assertEquals("L/kg", prePost.value("Vd/BW").unit)
        assertEquals("mg/L", prePost.value("Cmax").unit)
        assertEquals("mg/L", prePost.value("Cmin").unit)
        assertEquals("mg·h/L", prePost.value("AUC24").unit)
    }

    @Test
    fun prePostExplanationContainsApprovedCalculationSteps() {
        val result = calculator.calculate(prePostInput())

        assertExplanationTitles(
            result,
            "Two-point Elimination Denominator",
            "Patient-specific Elimination Rate Constant",
            "Half-life",
            "Extrapolated Peak Concentration",
            "Projected Trough Concentration",
            "Volume of Distribution",
            "Weight-normalised Volume of Distribution",
            "AUC Timing from Infusion Start",
            "Start-of-infusion Concentration",
            "Interval AUC",
            "Dosing Frequency per Day",
            "24-hour AUC"
        )
    }

    @Test
    fun missingRequiredDoseFailsWithControlledIssue() {
        val exception = expectCalculationException {
            calculator.calculate(preInput().copy(medicationDose = null))
        }

        assertTrue(exception.issues.isNotEmpty())
        assertTrue(exception.issues.any { it.field == "medicationDose" })
    }

    @Test
    fun nonPositiveDoseIsRejected() {
        listOf(0.0, -100.0).forEach { dose ->
            val exception = expectCalculationException {
                calculator.calculate(preInput().copy(medicationDose = dose))
            }
            assertTrue(exception.issues.any { it.field == "medicationDose" })
        }
    }

    @Test
    fun nonPositiveDosingIntervalIsRejected() {
        listOf(0.0, -12.0).forEach { interval ->
            val exception = expectCalculationException {
                calculator.calculate(preInput().copy(dosingInterval = interval))
            }
            assertTrue(exception.issues.any { it.field == "dosingInterval" })
        }
    }

    @Test
    fun preRequiresPatientAgeForCalculation() {
        val exception = expectCalculationException {
            calculator.calculate(
                preInput().copy(
                    patientParameters = mapOf(TdmInputKeys.BODY_WEIGHT_KG to "70")
                )
            )
        }

        assertTrue(exception.issues.any { it.field == TdmInputKeys.AGE_YEARS })
    }

    @Test
    fun malformedPatientAgeCannotReachCalculation() {
        val exception = expectCalculationException {
            calculator.calculate(
                preInput().copy(
                    patientParameters = mapOf(
                        TdmInputKeys.AGE_YEARS to "NaN",
                        TdmInputKeys.BODY_WEIGHT_KG to "70"
                    )
                )
            )
        }

        assertTrue(exception.issues.any { it.field == TdmInputKeys.AGE_YEARS })
    }

    @Test
    fun missingPatientWeightFailsCalculation() {
        val exception = expectCalculationException {
            calculator.calculate(
                preInput().copy(
                    patientParameters = mapOf(TdmInputKeys.AGE_YEARS to "50")
                )
            )
        }

        assertTrue(exception.issues.any { it.field == TdmInputKeys.BODY_WEIGHT_KG })
    }

    @Test
    fun prePostRejectsZeroWeight() {
        val exception = expectCalculationException {
            calculator.calculate(
                prePostInput().copy(
                    patientParameters = mapOf(TdmInputKeys.BODY_WEIGHT_KG to "0")
                )
            )
        }

        assertTrue(exception.issues.any { it.field == TdmInputKeys.BODY_WEIGHT_KG })
    }

    @Test
    fun requiredConcentrationsMustBePositive() {
        val cases = listOf(
            preInput().copy(preDoseConcentration = 0.0) to "preDoseConcentration",
            postInput().copy(postDoseConcentration = 0.0) to "postDoseConcentration",
            prePostInput().copy(preDoseConcentration = 0.0) to "preDoseConcentration"
        )

        cases.forEach { (input, field) ->
            val exception = expectCalculationException { calculator.calculate(input) }
            assertTrue(exception.issues.any { it.field == field })
        }
    }

    @Test
    fun prePostRejectsPostConcentrationNotGreaterThanPre() {
        listOf(15.9, 15.0).forEach { postConcentration ->
            val exception = expectCalculationException {
                calculator.calculate(
                    prePostInput().copy(postDoseConcentration = postConcentration)
                )
            }
            assertTrue(exception.issues.any { it.field == "postDoseConcentration" })
        }
    }

    @Test
    fun prePostRejectsNonPositiveTwoPointDenominator() {
        listOf(12.0, 13.0).forEach { sampleDifference ->
            val exception = expectCalculationException {
                calculator.calculate(
                    prePostInput().copy(
                        samplingInformation = prePostInput().samplingInformation +
                            (TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS to sampleDifference)
                    )
                )
            }
            assertTrue(
                exception.issues.any {
                    it.field == TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS
                }
            )
        }
    }

    @Test
    fun negativePostSampleDelayIsRejected() {
        val exception = expectCalculationException {
            calculator.calculate(
                postInput().copy(
                    samplingInformation = mapOf(
                        TdmInputKeys.POST_SAMPLE_DELAY_HOURS to -0.5
                    )
                )
            )
        }

        assertTrue(
            exception.issues.any { it.field == TdmInputKeys.POST_SAMPLE_DELAY_HOURS }
        )
    }

    @Test
    fun zeroPostSampleDelayIsPermitted() {
        val result = calculator.calculate(
            postInput().copy(
                samplingInformation = mapOf(TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 0.0)
            )
        )

        assertEquals(25.0, result.value("Cmax").value, 1e-12)
    }

    @Test
    fun postRequiresCreatinineClearanceForCalculation() {
        val exception = expectCalculationException {
            calculator.calculate(postInput().copy(creatinineClearanceMlMin = null))
        }

        assertTrue(exception.issues.any { it.field == "creatinineClearanceMlMin" })
    }

    @Test
    fun postRejectsNonFiniteCreatinineClearance() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY).forEach { crCl ->
            val exception = expectCalculationException {
                calculator.calculate(postInput().copy(creatinineClearanceMlMin = crCl))
            }
            assertTrue(exception.issues.any { it.field == "creatinineClearanceMlMin" })
        }
    }

    @Test
    fun prePostRequiresInfusionDurationForAuc() {
        val exception = expectCalculationException {
            calculator.calculate(prePostInput().copy(infusionDurationHours = null))
        }

        assertTrue(exception.issues.any { it.field == "infusionDurationHours" })
    }

    @Test
    fun prePostRejectsNonPositiveInfusionDuration() {
        listOf(0.0, -1.0).forEach { duration ->
            val exception = expectCalculationException {
                calculator.calculate(prePostInput().copy(infusionDurationHours = duration))
            }
            assertTrue(exception.issues.any { it.field == "infusionDurationHours" })
        }
    }

    @Test
    fun representativeNonFiniteValuesAreRejected() {
        val cases = listOf(
            preInput().copy(medicationDose = Double.NaN) to "medicationDose",
            preInput().copy(dosingInterval = Double.NEGATIVE_INFINITY) to "dosingInterval",
            postInput().copy(
                samplingInformation = mapOf(
                    TdmInputKeys.POST_SAMPLE_DELAY_HOURS to Double.POSITIVE_INFINITY
                )
            ) to TdmInputKeys.POST_SAMPLE_DELAY_HOURS
        )

        cases.forEach { (input, field) ->
            val exception = expectCalculationException { calculator.calculate(input) }
            assertTrue(exception.issues.any { it.field == field })
        }
    }

    private fun preInput(): TdmInput = TdmInput(
        workflow = WorkflowType.PRE,
        patientParameters = mapOf(
            TdmInputKeys.AGE_YEARS to "50",
            TdmInputKeys.BODY_WEIGHT_KG to "70"
        ),
        medicationDose = 1000.0,
        dosingInterval = 12.0,
        preDoseConcentration = 15.0
    )

    private fun postInput(): TdmInput = TdmInput(
        workflow = WorkflowType.POST,
        patientParameters = mapOf(
            TdmInputKeys.AGE_YEARS to "50",
            TdmInputKeys.BODY_WEIGHT_KG to "70"
        ),
        medicationDose = 1000.0,
        dosingInterval = 12.0,
        postDoseConcentration = 25.0,
        samplingInformation = mapOf(
            TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 1.5
        ),
        creatinineClearanceMlMin = 80.0
    )

    private fun prePostInput(): TdmInput = TdmInput(
        workflow = WorkflowType.PRE_POST,
        patientParameters = mapOf(TdmInputKeys.BODY_WEIGHT_KG to "70"),
        medicationDose = 750.0,
        dosingInterval = 12.0,
        preDoseConcentration = 15.9,
        postDoseConcentration = 29.3,
        samplingInformation = mapOf(
            TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 1.0,
            TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS to 2.5
        ),
        infusionDurationHours = 1.0
    )

    private fun TdmResult.value(label: String): TdmResultValue =
        (intermediateValues + pharmacokineticParameters + finalValues)
            .singleOrNull { it.label == label }
            ?: throw AssertionError("Expected result value '$label'.")

    private fun assertValue(
        result: TdmResult,
        label: String,
        expected: Double,
        delta: Double,
        expectedUnit: String
    ) {
        val actual = result.value(label)
        assertEquals(expected, actual.value, delta)
        assertEquals(expectedUnit, actual.unit)
    }

    private fun assertExplanationTitles(result: TdmResult, vararg expectedTitles: String) {
        val titles = result.explanation.map { it.title }
        expectedTitles.forEach { title ->
            assertTrue("Missing calculation explanation step: $title", title in titles)
        }
    }

    private fun expectCalculationException(block: () -> Unit): TdmCalculationException {
        try {
            block()
        } catch (exception: TdmCalculationException) {
            return exception
        }
        throw AssertionError("Expected TdmCalculationException.")
    }
}
