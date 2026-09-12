package com.example.tdminsight.validation

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.requirements

class TdmInputValidator {

    fun validate(draft: TdmInputDraft): ValidationResult {
        val parsingIssues = mutableListOf<ValidationIssue>()

        val medicationDose = parseOptionalFinite(
            field = "medicationDose",
            rawValue = draft.medicationDose,
            issues = parsingIssues
        )
        val dosingInterval = parseOptionalFinite(
            field = "dosingInterval",
            rawValue = draft.dosingInterval,
            issues = parsingIssues
        )
        val preDoseConcentration = parseOptionalFinite(
            field = "preDoseConcentration",
            rawValue = draft.preDoseConcentration,
            issues = parsingIssues
        )
        val postDoseConcentration = parseOptionalFinite(
            field = "postDoseConcentration",
            rawValue = draft.postDoseConcentration,
            issues = parsingIssues
        )
        val samplingTime = parseOptionalFinite(
            field = "samplingTime",
            rawValue = draft.samplingTime,
            issues = parsingIssues
        )
        val additionalTiming = parseOptionalFinite(
            field = "additionalTiming",
            rawValue = draft.additionalTimingInformation,
            issues = parsingIssues
        )

        if (parsingIssues.any { it.severity == ValidationSeverity.ERROR }) {
            return ValidationResult(issues = parsingIssues)
        }

        val samplingInformation = buildMap {
            samplingTime?.let { put("samplingTime", it) }
            additionalTiming?.let { put("additionalTiming", it) }
        }

        return validate(
            TdmInput(
                workflow = draft.workflow,
                patientParameters = draft.patientParameters,
                medicationDose = medicationDose,
                dosingInterval = dosingInterval,
                preDoseConcentration = preDoseConcentration,
                postDoseConcentration = postDoseConcentration,
                samplingInformation = samplingInformation,
                laboratoryInformation = draft.laboratoryInformation
            )
        )
    }

    fun validate(input: TdmInput): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val requirements = input.workflow.requirements()

        validateOptionalFiniteText("age", input.patientParameters["age"], issues)
        validateOptionalFiniteText("weight", input.patientParameters["weight"], issues)

        validateFiniteValue("medicationDose", input.medicationDose, issues)
        validateFiniteValue("dosingInterval", input.dosingInterval, issues)
        validateFiniteValue("preDoseConcentration", input.preDoseConcentration, issues)
        validateFiniteValue("postDoseConcentration", input.postDoseConcentration, issues)

        input.samplingInformation.forEach { (field, value) ->
            validateFiniteValue(field, value, issues)
        }

        validateWorkflowField(
            field = "preDoseConcentration",
            required = requirements.requiresPreConcentration,
            present = input.preDoseConcentration != null,
            issues = issues
        )
        validateWorkflowField(
            field = "postDoseConcentration",
            required = requirements.requiresPostConcentration,
            present = input.postDoseConcentration != null,
            issues = issues
        )
        validateWorkflowField(
            field = "samplingTime",
            required = requirements.requiresSamplingInformation,
            present = input.samplingInformation["samplingTime"] != null,
            issues = issues
        )
        validateWorkflowField(
            field = "additionalTiming",
            required = requirements.requiresAdditionalTimingInformation,
            present = input.samplingInformation["additionalTiming"] != null,
            issues = issues
        )

        val hasErrors = issues.any { it.severity == ValidationSeverity.ERROR }
        return ValidationResult(
            issues = issues,
            validatedInput = input.takeUnless { hasErrors }
        )
    }

    private fun parseOptionalFinite(
        field: String,
        rawValue: String,
        issues: MutableList<ValidationIssue>
    ): Double? {
        val normalized = rawValue.trim()
        if (normalized.isEmpty()) return null

        val parsed = normalized.toDoubleOrNull()
        if (parsed == null || !parsed.isFinite()) {
            issues += ValidationIssue(
                field = field,
                message = "Enter a finite numeric value.",
                severity = ValidationSeverity.ERROR
            )
            return null
        }

        return parsed
    }

    private fun validateOptionalFiniteText(
        field: String,
        rawValue: String?,
        issues: MutableList<ValidationIssue>
    ) {
        val normalized = rawValue?.trim().orEmpty()
        if (normalized.isEmpty()) return

        val parsed = normalized.toDoubleOrNull()
        if (parsed == null || !parsed.isFinite()) {
            issues += ValidationIssue(
                field = field,
                message = "Enter a finite numeric value.",
                severity = ValidationSeverity.ERROR
            )
        }
    }

    private fun validateFiniteValue(
        field: String,
        value: Double?,
        issues: MutableList<ValidationIssue>
    ) {
        if (value != null && !value.isFinite()) {
            issues += ValidationIssue(
                field = field,
                message = "Value must be finite.",
                severity = ValidationSeverity.ERROR
            )
        }
    }

    private fun validateWorkflowField(
        field: String,
        required: Boolean,
        present: Boolean,
        issues: MutableList<ValidationIssue>
    ) {
        when {
            required && !present -> issues += ValidationIssue(
                field = field,
                message = "This value is required for the selected workflow.",
                severity = ValidationSeverity.ERROR
            )

            !required && present -> issues += ValidationIssue(
                field = field,
                message = "This value is not required for the selected workflow; review whether it was entered intentionally.",
                severity = ValidationSeverity.REVIEW
            )
        }
    }
}
