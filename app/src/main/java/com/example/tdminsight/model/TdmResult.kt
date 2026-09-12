package com.example.tdminsight.model

data class TdmResultValue(
    val label: String,
    val value: Double,
    val unit: String? = null
)

data class CalculationStep(
    val title: String,
    val detail: String
)

data class TdmResult(
    val intermediateValues: List<TdmResultValue>,
    val pharmacokineticParameters: List<TdmResultValue>,
    val finalValues: List<TdmResultValue>,
    val explanation: List<CalculationStep>
)
