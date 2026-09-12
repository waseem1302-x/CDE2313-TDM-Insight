package com.example.tdminsight.validation

import com.example.tdminsight.model.TdmInput

data class ValidationResult(
    val issues: List<ValidationIssue>,
    val validatedInput: TdmInput? = null
) {
    val errors: List<ValidationIssue>
        get() = issues.filter { it.severity == ValidationSeverity.ERROR }

    val reviewItems: List<ValidationIssue>
        get() = issues.filter { it.severity == ValidationSeverity.REVIEW }

    val hasErrors: Boolean
        get() = errors.isNotEmpty()

    val canProceed: Boolean
        get() = !hasErrors
}
