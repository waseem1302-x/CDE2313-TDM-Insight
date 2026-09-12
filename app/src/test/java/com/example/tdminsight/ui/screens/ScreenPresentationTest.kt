package com.example.tdminsight.ui.screens

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.WorkflowType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenPresentationTest {

    @Test
    fun preShowsPreButNotPostOrSamplingFields() {
        val fields = calculationFieldsFor(WorkflowType.PRE)

        assertTrue(CalculationField.PRE_CONCENTRATION in fields)
        assertFalse(CalculationField.POST_CONCENTRATION in fields)
        assertFalse(CalculationField.SAMPLING_TIME in fields)
        assertFalse(CalculationField.ADDITIONAL_TIMING in fields)
    }

    @Test
    fun postShowsPostAndSamplingButNotPreOrAdditionalTiming() {
        val fields = calculationFieldsFor(WorkflowType.POST)

        assertFalse(CalculationField.PRE_CONCENTRATION in fields)
        assertTrue(CalculationField.POST_CONCENTRATION in fields)
        assertTrue(CalculationField.SAMPLING_TIME in fields)
        assertFalse(CalculationField.ADDITIONAL_TIMING in fields)
    }

    @Test
    fun prePostShowsBothConcentrationsAndTimingFields() {
        val fields = calculationFieldsFor(WorkflowType.PRE_POST)

        assertTrue(CalculationField.PRE_CONCENTRATION in fields)
        assertTrue(CalculationField.POST_CONCENTRATION in fields)
        assertTrue(CalculationField.SAMPLING_TIME in fields)
        assertTrue(CalculationField.ADDITIONAL_TIMING in fields)
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
}
