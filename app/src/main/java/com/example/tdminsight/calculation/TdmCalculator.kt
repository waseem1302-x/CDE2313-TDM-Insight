package com.example.tdminsight.calculation

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmResult

interface TdmCalculator {
    fun calculate(input: TdmInput): TdmResult
}
