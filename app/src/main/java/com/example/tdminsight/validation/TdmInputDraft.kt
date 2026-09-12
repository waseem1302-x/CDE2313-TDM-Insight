package com.example.tdminsight.validation

import com.example.tdminsight.model.WorkflowType

data class TdmInputDraft(
    val workflow: WorkflowType,
    val patientParameters: Map<String, String> = emptyMap(),
    val medicationDose: String = "",
    val dosingInterval: String = "",
    val preDoseConcentration: String = "",
    val postDoseConcentration: String = "",
    val samplingTime: String = "",
    val additionalTimingInformation: String = "",
    val laboratoryInformation: Map<String, String> = emptyMap()
)
