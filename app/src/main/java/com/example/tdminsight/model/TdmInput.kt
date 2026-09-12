package com.example.tdminsight.model

data class TdmInput(
    val workflow: WorkflowType,
    val patientParameters: Map<String, String> = emptyMap(),
    val medicationDose: Double? = null,
    val dosingInterval: Double? = null,
    val preDoseConcentration: Double? = null,
    val postDoseConcentration: Double? = null,
    val samplingInformation: Map<String, Double> = emptyMap(),
    val laboratoryInformation: Map<String, String> = emptyMap(),
    val creatinineClearanceMlMin: Double? = null,
    val infusionDurationHours: Double? = null
)
