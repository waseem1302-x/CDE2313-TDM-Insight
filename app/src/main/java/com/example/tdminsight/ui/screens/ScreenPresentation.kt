package com.example.tdminsight.ui.screens

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.WorkflowType
import com.example.tdminsight.model.requirements

enum class CalculationField {
    MEDICATION_DOSE,
    DOSING_INTERVAL,
    PRE_CONCENTRATION,
    POST_CONCENTRATION,
    SAMPLING_TIME,
    ADDITIONAL_TIMING,
    LAB_NOTE
}

data class ReviewItem(
    val label: String,
    val value: String
)

fun calculationFieldsFor(workflow: WorkflowType): List<CalculationField> {
    val requirements = workflow.requirements()
    return buildList {
        add(CalculationField.MEDICATION_DOSE)
        add(CalculationField.DOSING_INTERVAL)
        if (requirements.requiresPreConcentration) add(CalculationField.PRE_CONCENTRATION)
        if (requirements.requiresPostConcentration) add(CalculationField.POST_CONCENTRATION)
        if (requirements.requiresSamplingInformation) add(CalculationField.SAMPLING_TIME)
        if (requirements.requiresAdditionalTimingInformation) add(CalculationField.ADDITIONAL_TIMING)
        add(CalculationField.LAB_NOTE)
    }
}

fun buildReviewItems(input: TdmInput): List<ReviewItem> = buildList {
    add(ReviewItem("Workflow", input.workflow.displayName))

    input.patientParameters.forEach { (key, value) ->
        if (value.isNotBlank()) add(ReviewItem(patientLabel(key), value))
    }

    input.medicationDose?.let { add(ReviewItem("Medication dose", it.toString())) }
    input.dosingInterval?.let { add(ReviewItem("Dosing interval", it.toString())) }
    input.preDoseConcentration?.let { add(ReviewItem("Pre-dose concentration", it.toString())) }
    input.postDoseConcentration?.let { add(ReviewItem("Post-dose concentration", it.toString())) }

    input.samplingInformation.forEach { (key, value) ->
        add(ReviewItem(samplingLabel(key), value.toString()))
    }

    input.laboratoryInformation.forEach { (key, value) ->
        if (value.isNotBlank()) add(ReviewItem(laboratoryLabel(key), value))
    }
}

private fun patientLabel(key: String): String = when (key) {
    "caseId" -> "Case ID"
    "age" -> "Age"
    "weight" -> "Weight"
    "notes" -> "Case notes"
    else -> key.toReadableLabel()
}

private fun samplingLabel(key: String): String = when (key) {
    "samplingTime" -> "Sampling time"
    "additionalTiming" -> "Additional timing information"
    else -> key.toReadableLabel()
}

private fun laboratoryLabel(key: String): String = when (key) {
    "notes" -> "Laboratory note"
    else -> key.toReadableLabel()
}

private fun String.toReadableLabel(): String =
    replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace('_', ' ')
        .trim()
        .replaceFirstChar { character -> character.uppercase() }
