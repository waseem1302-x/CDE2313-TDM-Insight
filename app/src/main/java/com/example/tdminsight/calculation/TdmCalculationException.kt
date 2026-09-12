package com.example.tdminsight.calculation

import com.example.tdminsight.validation.ValidationIssue

class TdmCalculationException(
    val issues: List<ValidationIssue>
) : IllegalArgumentException(
    issues.joinToString(separator = "; ") { issue ->
        "${issue.field}: ${issue.message}"
    }
)
