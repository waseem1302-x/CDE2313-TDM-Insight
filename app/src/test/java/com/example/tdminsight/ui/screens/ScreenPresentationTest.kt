package com.example.tdminsight.ui.screens

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.WorkflowType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenPresentationTest {

    @Test
    fun preShowsPreButNotPostOrSamplingFields() {
        val fields = calculationFieldsFor(WorkflowType.PRE)

        assertTrue(CalculationField.MEDICATION_DOSE in fields)
        assertTrue(CalculationField.DOSING_INTERVAL in fields)
        assertTrue(CalculationField.PRE_CONCENTRATION in fields)
        assertFalse(CalculationField.POST_CONCENTRATION in fields)
        assertFalse(CalculationField.SAMPLING_TIME in fields)
        assertFalse(CalculationField.ADDITIONAL_TIMING in fields)
        assertFalse(CalculationField.CREATININE_CLEARANCE in fields)
        assertFalse(CalculationField.INFUSION_DURATION in fields)
    }

    @Test
    fun postShowsPostAndSamplingButNotPreOrAdditionalTiming() {
        val fields = calculationFieldsFor(WorkflowType.POST)

        assertTrue(CalculationField.MEDICATION_DOSE in fields)
        assertTrue(CalculationField.DOSING_INTERVAL in fields)
        assertFalse(CalculationField.PRE_CONCENTRATION in fields)
        assertTrue(CalculationField.POST_CONCENTRATION in fields)
        assertTrue(CalculationField.SAMPLING_TIME in fields)
        assertFalse(CalculationField.ADDITIONAL_TIMING in fields)
        assertTrue(CalculationField.CREATININE_CLEARANCE in fields)
        assertFalse(CalculationField.INFUSION_DURATION in fields)
    }

    @Test
    fun prePostShowsBothConcentrationsAndTimingFields() {
        val fields = calculationFieldsFor(WorkflowType.PRE_POST)

        assertTrue(CalculationField.MEDICATION_DOSE in fields)
        assertTrue(CalculationField.DOSING_INTERVAL in fields)
        assertTrue(CalculationField.PRE_CONCENTRATION in fields)
        assertTrue(CalculationField.POST_CONCENTRATION in fields)
        assertTrue(CalculationField.SAMPLING_TIME in fields)
        assertTrue(CalculationField.ADDITIONAL_TIMING in fields)
        assertFalse(CalculationField.CREATININE_CLEARANCE in fields)
        assertTrue(CalculationField.INFUSION_DURATION in fields)
    }

    @Test
    fun reviewItemsHideBlankAndMissingValues() {
        val items = buildReviewItems(
            TdmInput(
                workflow = WorkflowType.PRE,
                patientParameters = mapOf(
                    "caseId" to "CASE-01",
                    "notes" to ""
                ),
                medicationDose = 500.0,
                preDoseConcentration = 12.5,
                laboratoryInformation = mapOf("notes" to "")
            )
        )

        assertEquals("Vancomycin Pre", items.first().value)
        assertTrue(items.any { it.label == "Case ID" && it.value == "CASE-01" })
        assertTrue(items.any { it.label == "Medication dose" && it.value == "500.0" })
        assertTrue(items.any { it.label == "Pre-dose concentration" && it.value == "12.5" })
        assertFalse(items.any { it.value.isBlank() })
        assertFalse(items.any { it.label == "Laboratory note" })
    }

    @Test
    fun reviewItemsIncludeCalculationSpecificValuesAndUnits() {
        val items = buildReviewItems(
            TdmInput(
                workflow = WorkflowType.PRE_POST,
                patientParameters = mapOf(
                    TdmInputKeys.AGE_YEARS to "50",
                    TdmInputKeys.BODY_WEIGHT_KG to "70"
                ),
                medicationDose = 750.0,
                dosingInterval = 12.0,
                preDoseConcentration = 15.9,
                postDoseConcentration = 29.3,
                samplingInformation = mapOf(
                    TdmInputKeys.POST_SAMPLE_DELAY_HOURS to 1.0,
                    TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS to 2.5
                ),
                creatinineClearanceMlMin = 80.0,
                infusionDurationHours = 1.0
            )
        )

        assertTrue(items.any { it.label == "Age (years)" && it.value == "50" })
        assertTrue(items.any { it.label == "Weight (kg)" && it.value == "70" })
        assertTrue(items.any { it.label == "Creatinine clearance (mL/min)" && it.value == "80.0" })
        assertTrue(items.any { it.label == "Infusion duration (hours)" && it.value == "1.0" })
        assertTrue(
            items.any {
                it.label == "Post sample — hours after infusion end" && it.value == "1.0"
            }
        )
        assertTrue(
            items.any {
                it.label == "Pre/Post sample time difference (hours)" && it.value == "2.5"
            }
        )
    }
}
