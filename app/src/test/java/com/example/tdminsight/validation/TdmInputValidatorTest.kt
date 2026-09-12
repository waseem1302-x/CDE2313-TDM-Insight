package com.example.tdminsight.validation

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.WorkflowType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TdmInputValidatorTest {

    private val validator = TdmInputValidator()

    @Test
    fun preAcceptsRequiredPreConcentrationWhenPresent() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                preDoseConcentration = "12.5"
            )
        )

        assertFalse(result.hasErrors)
        assertTrue(result.canProceed)
        assertNotNull(result.validatedInput)
        assertEquals(12.5, result.validatedInput?.preDoseConcentration ?: 0.0, 0.0)
    }

    @Test
    fun preReportsMissingPreConcentration() {
        val result = validator.validate(TdmInputDraft(workflow = WorkflowType.PRE))

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "preDoseConcentration" })
        assertNull(result.validatedInput)
    }

    @Test
    fun preDoesNotRequirePostOnlyFields() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                preDoseConcentration = "10"
            )
        )

        assertFalse(result.errors.any { it.field == "postDoseConcentration" })
        assertFalse(result.errors.any { it.field == "samplingTime" })
        assertFalse(result.errors.any { it.field == "additionalTiming" })
    }

    @Test
    fun postRequiresPostConcentrationAndSamplingInformation() {
        val result = validator.validate(TdmInputDraft(workflow = WorkflowType.POST))

        assertTrue(result.errors.any { it.field == "postDoseConcentration" })
        assertTrue(result.errors.any { it.field == "samplingTime" })
        assertFalse(result.errors.any { it.field == "preDoseConcentration" })
        assertFalse(result.errors.any { it.field == "additionalTiming" })
    }

    @Test
    fun postAcceptsRequiredValuesWithoutPreConcentration() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.POST,
                postDoseConcentration = "18.2",
                samplingTime = "1.5"
            )
        )

        assertFalse(result.hasErrors)
        assertNull(result.validatedInput?.preDoseConcentration)
        assertEquals(18.2, result.validatedInput?.postDoseConcentration ?: 0.0, 0.0)
        assertEquals(1.5, result.validatedInput?.samplingInformation?.get("samplingTime") ?: 0.0, 0.0)
    }

    @Test
    fun prePostRequiresBothConcentrationsAndAllTimingInformation() {
        val result = validator.validate(TdmInputDraft(workflow = WorkflowType.PRE_POST))

        assertTrue(result.errors.any { it.field == "preDoseConcentration" })
        assertTrue(result.errors.any { it.field == "postDoseConcentration" })
        assertTrue(result.errors.any { it.field == "samplingTime" })
        assertTrue(result.errors.any { it.field == "additionalTiming" })
    }

    @Test
    fun prePostAcceptsAllWorkflowRequiredValues() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE_POST,
                preDoseConcentration = "9.4",
                postDoseConcentration = "21.7",
                samplingTime = "2",
                additionalTimingInformation = "8"
            )
        )

        assertFalse(result.hasErrors)
        assertNotNull(result.validatedInput)
        assertEquals(8.0, result.validatedInput?.samplingInformation?.get("additionalTiming") ?: 0.0, 0.0)
    }

    @Test
    fun malformedNumericInputProducesBlockingErrorWithoutThrowing() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                preDoseConcentration = "not-a-number"
            )
        )

        assertTrue(result.hasErrors)
        assertFalse(result.canProceed)
        assertTrue(result.errors.any { it.field == "preDoseConcentration" })
        assertNull(result.validatedInput)
    }

    @Test
    fun nonNumericAgeProducesBlockingError() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                patientParameters = mapOf("age" to "abc"),
                preDoseConcentration = "12"
            )
        )

        assertTrue(result.hasErrors)
        assertFalse(result.canProceed)
        assertTrue(result.errors.any { it.field == "age" })
        assertNull(result.validatedInput)
    }

    @Test
    fun nonNumericWeightProducesBlockingError() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                patientParameters = mapOf("weight" to "not-a-number"),
                preDoseConcentration = "12"
            )
        )

        assertTrue(result.hasErrors)
        assertFalse(result.canProceed)
        assertTrue(result.errors.any { it.field == "weight" })
        assertNull(result.validatedInput)
    }

    @Test
    fun nonFinitePatientNumericTextProducesBlockingErrors() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                patientParameters = mapOf(
                    "age" to "NaN",
                    "weight" to "Infinity"
                ),
                preDoseConcentration = "12"
            )
        )

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "age" })
        assertTrue(result.errors.any { it.field == "weight" })
        assertNull(result.validatedInput)
    }

    @Test
    fun validNumericAgeAndWeightDoNotCreateBlockingErrors() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE,
                patientParameters = mapOf(
                    "age" to "42",
                    "weight" to "73.5"
                ),
                preDoseConcentration = "12"
            )
        )

        assertFalse(result.hasErrors)
        assertTrue(result.canProceed)
        assertEquals("42", result.validatedInput?.patientParameters?.get("age"))
        assertEquals("73.5", result.validatedInput?.patientParameters?.get("weight"))
    }

    @Test
    fun nonFiniteNumericInputProducesBlockingError() {
        val result = validator.validate(
            TdmInput(
                workflow = WorkflowType.PRE,
                preDoseConcentration = Double.NaN
            )
        )

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "preDoseConcentration" })
        assertFalse(result.canProceed)
    }

    @Test
    fun validTypedInputHasNoBlockingErrors() {
        val result = validator.validate(
            TdmInput(
                workflow = WorkflowType.POST,
                postDoseConcentration = 16.0,
                samplingInformation = mapOf("samplingTime" to 2.0)
            )
        )

        assertFalse(result.hasErrors)
        assertTrue(result.canProceed)
        assertEquals(0, result.errors.size)
    }

    @Test
    fun extraWorkflowFieldProducesReviewIssueWithoutBlocking() {
        val result = validator.validate(
            TdmInput(
                workflow = WorkflowType.PRE,
                preDoseConcentration = 11.0,
                postDoseConcentration = 20.0
            )
        )

        assertFalse(result.hasErrors)
        assertTrue(result.canProceed)
        assertTrue(
            result.reviewItems.any {
                it.field == "postDoseConcentration" &&
                    it.severity == ValidationSeverity.REVIEW
            }
        )
    }

    @Test
    fun emptyDraftReturnsIssuesInsteadOfCrashing() {
        val result = validator.validate(TdmInputDraft(workflow = WorkflowType.PRE_POST))

        assertTrue(result.hasErrors)
        assertTrue(result.issues.isNotEmpty())
        assertNull(result.validatedInput)
    }

    @Test
    fun postStructuralValidationRemainsBackwardCompatibleWithoutCrCl() {
        val result = validator.validate(
            TdmInput(
                workflow = WorkflowType.POST,
                postDoseConcentration = 25.0,
                samplingInformation = mapOf(
                    TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 1.5
                )
            )
        )

        assertFalse(result.hasErrors)
        assertNotNull(result.validatedInput)
        assertFalse(result.errors.any { it.field == "creatinineClearanceMlMin" })
    }

    @Test
    fun postRequiresCreatinineClearanceAtCalculationGate() {
        val result = validator.validateForCalculation(
            TdmInput(
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
                )
            )
        )

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "creatinineClearanceMlMin" })
        assertNull(result.validatedInput)
    }

    @Test
    fun prePostStructuralValidationRemainsBackwardCompatibleWithoutInfusionDuration() {
        val result = validator.validate(
            TdmInput(
                workflow = WorkflowType.PRE_POST,
                preDoseConcentration = 15.9,
                postDoseConcentration = 29.3,
                samplingInformation = mapOf(
                    TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 1.0,
                    TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS to 2.5
                )
            )
        )

        assertFalse(result.hasErrors)
        assertNotNull(result.validatedInput)
        assertFalse(result.errors.any { it.field == "infusionDurationHours" })
    }

    @Test
    fun prePostRequiresInfusionDurationAtCalculationGate() {
        val result = validator.validateForCalculation(
            TdmInput(
                workflow = WorkflowType.PRE_POST,
                patientParameters = mapOf(TdmInputKeys.BODY_WEIGHT_KG to "70"),
                medicationDose = 750.0,
                dosingInterval = 12.0,
                preDoseConcentration = 15.9,
                postDoseConcentration = 29.3,
                samplingInformation = mapOf(
                    TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 1.0,
                    TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS to 2.5
                )
            )
        )

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "infusionDurationHours" })
        assertNull(result.validatedInput)
    }

    @Test
    fun postDraftParsesCreatinineClearance() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.POST,
                postDoseConcentration = "25",
                samplingTime = "1.5",
                creatinineClearanceMlMin = "80"
            )
        )

        assertFalse(result.hasErrors)
        assertEquals(80.0, result.validatedInput?.creatinineClearanceMlMin ?: 0.0, 0.0)
    }

    @Test
    fun prePostDraftParsesInfusionDuration() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE_POST,
                preDoseConcentration = "15.9",
                postDoseConcentration = "29.3",
                samplingTime = "1",
                additionalTimingInformation = "2.5",
                infusionDurationHours = "1"
            )
        )

        assertFalse(result.hasErrors)
        assertEquals(1.0, result.validatedInput?.infusionDurationHours ?: 0.0, 0.0)
    }

    @Test
    fun malformedCreatinineClearanceDraftProducesBlockingIssue() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.POST,
                postDoseConcentration = "25",
                samplingTime = "1.5",
                creatinineClearanceMlMin = "abc"
            )
        )

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "creatinineClearanceMlMin" })
        assertNull(result.validatedInput)
    }

    @Test
    fun nonFiniteNewDraftFieldsProduceBlockingIssues() {
        val result = validator.validate(
            TdmInputDraft(
                workflow = WorkflowType.PRE_POST,
                preDoseConcentration = "15.9",
                postDoseConcentration = "29.3",
                samplingTime = "1",
                additionalTimingInformation = "2.5",
                creatinineClearanceMlMin = "Infinity",
                infusionDurationHours = "NaN"
            )
        )

        assertTrue(result.hasErrors)
        assertTrue(result.errors.any { it.field == "creatinineClearanceMlMin" })
        assertTrue(result.errors.any { it.field == "infusionDurationHours" })
        assertNull(result.validatedInput)
    }
}
