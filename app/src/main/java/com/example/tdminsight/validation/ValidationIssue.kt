package com.example.tdminsight.validation

data class ValidationIssue(
    val field: String,
    val message: String,
    val severity: ValidationSeverity
)
