package com.example.tdminsight.validation

import com.example.tdminsight.model.TdmInput
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.WorkflowType
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
            field = TdmInputKeys.POST_SAMPLE_DELAY_HOURS,
            rawValue = draft.samplingTime,
            issues = parsingIssues
        )
        val additionalTiming = parseOptionalFinite(
            field = TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS,
            rawValue = draft.additionalTimingInformation,
            issues = parsingIssues
        )
        val creatinineClearanceMlMin = parseOptionalFinite(
            field = "creatinineClearanceMlMin",
            rawValue = draft.creatinineClearanceMlMin,
            issues = parsingIssues
        )
        val infusionDurationHours = parseOptionalFinite(
            field = "infusionDurationHours",
            rawValue = draft.infusionDurationHours,
            issues = parsingIssues
        )

        if (parsingIssues.any { it.severity == ValidationSeverity.ERROR }) {
            return ValidationResult(issues = parsingIssues)
        }

        val samplingInformation = buildMap {
            samplingTime?.let { put(TdmInputKeys.POST_SAMPLE_DELAY_HOURS, it) }
            additionalTiming?.let { put(TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS, it) }
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
                laboratoryInformation = draft.laboratoryInformation,
                creatinineClearanceMlMin = creatinineClearanceMlMin,
                infusionDurationHours = infusionDurationHours
            )
        )
    }

    fun validate(input: TdmInput): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        val requirements = input.workflow.requirements()

        validateOptionalFiniteText(
            TdmInputKeys.AGE_YEARS,
            input.patientParameters[TdmInputKeys.AGE_YEARS],
            issues
        )
        validateOptionalFiniteText(
            TdmInputKeys.BODY_WEIGHT_KG,
            input.patientParameters[TdmInputKeys.BODY_WEIGHT_KG],
            issues
        )

        validateFiniteValue("medicationDose", input.medicationDose, issues)
        validateFiniteValue("dosingInterval", input.dosingInterval, issues)
        validateFiniteValue("preDoseConcentration", input.preDoseConcentration, issues)
        validateFiniteValue("postDoseConcentration", input.postDoseConcentration, issues)
        validateFiniteValue("creatinineClearanceMlMin", input.creatinineClearanceMlMin, issues)
        validateFiniteValue("infusionDurationHours", input.infusionDurationHours, issues)

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
            field = TdmInputKeys.POST_SAMPLE_DELAY_HOURS,
            required = requirements.requiresSamplingInformation,
            present = input.samplingInformation[TdmInputKeys.POST_SAMPLE_DELAY_HOURS] != null,
            issues = issues
        )
        validateWorkflowField(
            field = TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS,
            required = requirements.requiresAdditionalTimingInformation,
            present = input.samplingInformation[TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS] != null,
            issues = issues
        )
        validateOptionalWorkflowField(
            field = "creatinineClearanceMlMin",
            requiredForCalculation = requirements.requiresCreatinineClearance,
            present = input.creatinineClearanceMlMin != null,
            issues = issues
        )
        validateOptionalWorkflowField(
            field = "infusionDurationHours",
            requiredForCalculation = requirements.requiresInfusionDuration,
            present = input.infusionDurationHours != null,
            issues = issues
        )

        val hasErrors = issues.any { it.severity == ValidationSeverity.ERROR }
        return ValidationResult(
            issues = issues,
            validatedInput = input.takeUnless { hasErrors }
        )
    }

    fun validateForCalculation(input: TdmInput): ValidationResult {
        val structural = validate(input)
        if (structural.hasErrors) return structural

        val issues = structural.issues.toMutableList()
        val requirements = input.workflow.requirements()

        val medicationDose = requireFiniteValue(
            field = "medicationDose",
            value = input.medicationDose,
            issues = issues
        )
        val dosingInterval = requireFiniteValue(
            field = "dosingInterval",
            value = input.dosingInterval,
            issues = issues
        )
        val bodyWeight = requireFinitePatientValue(
            field = TdmInputKeys.BODY_WEIGHT_KG,
            rawValue = input.patientParameters[TdmInputKeys.BODY_WEIGHT_KG],
            issues = issues
        )

        if (input.workflow == WorkflowType.PRE || input.workflow == WorkflowType.POST) {
            requireFinitePatientValue(
                field = TdmInputKeys.AGE_YEARS,
                rawValue = input.patientParameters[TdmInputKeys.AGE_YEARS],
                issues = issues
            )
        }

        requirePositive("medicationDose", medicationDose, issues)
        requirePositive("dosingInterval", dosingInterval, issues)

        val preConcentration = input.preDoseConcentration
        val postConcentration = input.postDoseConcentration

        if (requirements.requiresPreConcentration) {
            requirePositive("preDoseConcentration", preConcentration, issues)
        }
        if (requirements.requiresPostConcentration) {
            requirePositive("postDoseConcentration", postConcentration, issues)
        }

        val postSampleDelay = input.samplingInformation[TdmInputKeys.POST_SAMPLE_DELAY_HOURS]
        if (requirements.requiresSamplingInformation) {
            requireNonNegative(
                TdmInputKeys.POST_SAMPLE_DELAY_HOURS,
                postSampleDelay,
                issues
            )
        }

        if (requirements.requiresCreatinineClearance) {
            requireFiniteValue(
                field = "creatinineClearanceMlMin",
                value = input.creatinineClearanceMlMin,
                issues = issues
            )
        }

        val sampleDifference = input.samplingInformation[TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS]
        if (input.workflow == WorkflowType.PRE_POST) {
            requirePositive(TdmInputKeys.BODY_WEIGHT_KG, bodyWeight, issues)

            if (preConcentration != null && postConcentration != null && postConcentration <= preConcentration) {
                addError(
                    field = "postDoseConcentration",
                    message = "Post-dose concentration must be greater than pre-dose concentration for the approved two-point equation.",
                    issues = issues
                )
            }

            if (dosingInterval != null && sampleDifference != null) {
                val denominator = dosingInterval - sampleDifference
                if (!denominator.isFinite() || denominator <= 0.0) {
                    addError(
                        field = TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS,
                        message = "Dosing interval minus the pre/post sample time difference must be greater than zero.",
                        issues = issues
                    )
                }
            }
        }

        if (requirements.requiresInfusionDuration) {
            val infusionDuration = requireFiniteValue(
                field = "infusionDurationHours",
                value = input.infusionDurationHours,
                issues = issues
            )
            requirePositive("infusionDurationHours", infusionDuration, issues)
        }

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
            addError(field, "Enter a finite numeric value.", issues)
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
            addError(field, "Enter a finite numeric value.", issues)
        }
    }

    private fun validateFiniteValue(
        field: String,
        value: Double?,
        issues: MutableList<ValidationIssue>
    ) {
        if (value != null && !value.isFinite()) {
            addError(field, "Value must be finite.", issues)
        }
    }

    private fun requireFiniteValue(
        field: String,
        value: Double?,
        issues: MutableList<ValidationIssue>
    ): Double? {
        if (value == null) {
            addError(field, "This value is required for calculation.", issues)
            return null
        }
        if (!value.isFinite()) {
            addError(field, "Value must be finite.", issues)
            return null
        }
        return value
    }

    private fun requireFinitePatientValue(
        field: String,
        rawValue: String?,
        issues: MutableList<ValidationIssue>
    ): Double? {
        val normalized = rawValue?.trim().orEmpty()
        if (normalized.isEmpty()) {
            addError(field, "This value is required for calculation.", issues)
            return null
        }

        val parsed = normalized.toDoubleOrNull()
        if (parsed == null || !parsed.isFinite()) {
            addError(field, "Enter a finite numeric value.", issues)
            return null
        }

        return parsed
    }

    private fun requirePositive(
        field: String,
        value: Double?,
        issues: MutableList<ValidationIssue>
    ) {
        if (value != null && value <= 0.0) {
            addError(field, "Value must be greater than zero for the approved equation.", issues)
        }
    }

    private fun requireNonNegative(
        field: String,
        value: Double?,
        issues: MutableList<ValidationIssue>
    ) {
        if (value != null && value < 0.0) {
            addError(field, "Value must be zero or greater for the approved equation.", issues)
        }
    }

    private fun validateWorkflowField(
        field: String,
        required: Boolean,
        present: Boolean,
        issues: MutableList<ValidationIssue>
    ) {
        when {
            required && !present -> addError(
                field,
                "This value is required for the selected workflow.",
                issues
            )

            !required && present -> issues += ValidationIssue(
                field = field,
                message = "This value is not required for the selected workflow; review whether it was entered intentionally.",
                severity = ValidationSeverity.REVIEW
            )
        }
    }

    private fun validateOptionalWorkflowField(
        field: String,
        requiredForCalculation: Boolean,
        present: Boolean,
        issues: MutableList<ValidationIssue>
    ) {
        if (!requiredForCalculation && present) {
            issues += ValidationIssue(
                field = field,
                message = "This value is not required for the selected workflow; review whether it was entered intentionally.",
                severity = ValidationSeverity.REVIEW
            )
        }
    }

    private fun addError(
        field: String,
        message: String,
        issues: MutableList<ValidationIssue>
    ) {
        issues += ValidationIssue(
            field = field,
            message = message,
            severity = ValidationSeverity.ERROR
        )
    }
}
