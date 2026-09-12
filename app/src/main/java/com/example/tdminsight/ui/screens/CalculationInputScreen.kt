package com.example.tdminsight.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.model.WorkflowType
import com.example.tdminsight.ui.components.TdmTextField
import com.example.tdminsight.ui.components.ValidationSummaryCard
import com.example.tdminsight.validation.ValidationIssue
import com.example.tdminsight.validation.ValidationSeverity

@Composable
fun CalculationInputScreen(
    workflow: WorkflowType,
    medicationDose: String,
    dosingInterval: String,
    preDoseConcentration: String,
    postDoseConcentration: String,
    samplingTime: String,
    additionalTimingInformation: String,
    laboratoryNote: String,
    onMedicationDoseChange: (String) -> Unit,
    onDosingIntervalChange: (String) -> Unit,
    onPreDoseConcentrationChange: (String) -> Unit,
    onPostDoseConcentrationChange: (String) -> Unit,
    onSamplingTimeChange: (String) -> Unit,
    onAdditionalTimingInformationChange: (String) -> Unit,
    onLaboratoryNoteChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    creatinineClearanceMlMin: String = "",
    infusionDurationHours: String = "",
    validationIssues: List<ValidationIssue> = emptyList(),
    onCreatinineClearanceMlMinChange: (String) -> Unit = {},
    onInfusionDurationHoursChange: (String) -> Unit = {}
) {
    val visibleFields = calculationFieldsFor(workflow)
    val fieldErrors = validationIssues
        .filter { it.severity == ValidationSeverity.ERROR }
        .associate { it.field to it.message }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = workflow.displayName,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Enter only the values needed for this workflow. All values are validated before the case can proceed to Review.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ValidationSummaryCard(validationIssues)

        SectionTitle("Dosage")
        TdmTextField(
            value = medicationDose,
            onValueChange = onMedicationDoseChange,
            label = "Medication dose (mg)",
            keyboardType = KeyboardType.Decimal,
            errorText = fieldErrors["medicationDose"]
        )
        TdmTextField(
            value = dosingInterval,
            onValueChange = onDosingIntervalChange,
            label = "Dosing interval (hours)",
            keyboardType = KeyboardType.Decimal,
            errorText = fieldErrors["dosingInterval"]
        )

        if (
            CalculationField.PRE_CONCENTRATION in visibleFields ||
            CalculationField.POST_CONCENTRATION in visibleFields
        ) {
            SectionTitle("Concentrations")
        }
        if (CalculationField.PRE_CONCENTRATION in visibleFields) {
            TdmTextField(
                value = preDoseConcentration,
                onValueChange = onPreDoseConcentrationChange,
                label = "Pre-dose concentration (mg/L)",
                keyboardType = KeyboardType.Decimal,
                errorText = fieldErrors["preDoseConcentration"]
            )
        }
        if (CalculationField.POST_CONCENTRATION in visibleFields) {
            TdmTextField(
                value = postDoseConcentration,
                onValueChange = onPostDoseConcentrationChange,
                label = "Post-dose concentration (mg/L)",
                keyboardType = KeyboardType.Decimal,
                errorText = fieldErrors["postDoseConcentration"]
            )
        }

        if (
            CalculationField.SAMPLING_TIME in visibleFields ||
            CalculationField.ADDITIONAL_TIMING in visibleFields ||
            CalculationField.INFUSION_DURATION in visibleFields
        ) {
            SectionTitle("Timing")
        }
        if (CalculationField.SAMPLING_TIME in visibleFields) {
            TdmTextField(
                value = samplingTime,
                onValueChange = onSamplingTimeChange,
                label = "Post sample — hours after infusion end",
                keyboardType = KeyboardType.Decimal,
                supportingText = "Time from the end of infusion to the post-dose blood sample.",
                errorText = fieldErrors[TdmInputKeys.POST_SAMPLE_DELAY_HOURS]
            )
        }
        if (CalculationField.ADDITIONAL_TIMING in visibleFields) {
            TdmTextField(
                value = additionalTimingInformation,
                onValueChange = onAdditionalTimingInformationChange,
                label = "Pre/Post sample time difference (hours)",
                keyboardType = KeyboardType.Decimal,
                supportingText = "Elapsed clock-time difference between the pre and post sample timestamps.",
                errorText = fieldErrors[TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS]
            )
        }
        if (CalculationField.INFUSION_DURATION in visibleFields) {
            TdmTextField(
                value = infusionDurationHours,
                onValueChange = onInfusionDurationHoursChange,
                label = "Infusion duration (hours)",
                keyboardType = KeyboardType.Decimal,
                errorText = fieldErrors["infusionDurationHours"]
            )
        }

        if (CalculationField.CREATININE_CLEARANCE in visibleFields) {
            SectionTitle("Renal Input")
            TdmTextField(
                value = creatinineClearanceMlMin,
                onValueChange = onCreatinineClearanceMlMinChange,
                label = "Creatinine clearance (mL/min)",
                keyboardType = KeyboardType.Decimal,
                errorText = fieldErrors["creatinineClearanceMlMin"]
            )
        }

        if (CalculationField.LAB_NOTE in visibleFields) {
            SectionTitle("Notes")
            TdmTextField(
                value = laboratoryNote,
                onValueChange = onLaboratoryNoteChange,
                label = "Laboratory note (optional)",
                singleLine = false
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue to Review")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 6.dp)
    )
}
