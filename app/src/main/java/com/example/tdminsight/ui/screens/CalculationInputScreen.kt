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
import com.example.tdminsight.model.WorkflowType
import com.example.tdminsight.ui.components.TdmTextField

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
    onCreatinineClearanceMlMinChange: (String) -> Unit = {},
    onInfusionDurationHoursChange: (String) -> Unit = {}
) {
    val visibleFields = calculationFieldsFor(workflow)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = workflow.displayName,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Enter only the values needed for this workflow. Values are reviewed and validated outside this screen before calculation.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (CalculationField.MEDICATION_DOSE in visibleFields) {
            TdmTextField(
                value = medicationDose,
                onValueChange = onMedicationDoseChange,
                label = "Medication dose (mg)",
                keyboardType = KeyboardType.Decimal
            )
        }

        if (CalculationField.DOSING_INTERVAL in visibleFields) {
            TdmTextField(
                value = dosingInterval,
                onValueChange = onDosingIntervalChange,
                label = "Dosing interval (hours)",
                keyboardType = KeyboardType.Decimal
            )
        }

        if (CalculationField.PRE_CONCENTRATION in visibleFields) {
            TdmTextField(
                value = preDoseConcentration,
                onValueChange = onPreDoseConcentrationChange,
                label = "Pre-dose concentration (mg/L)",
                keyboardType = KeyboardType.Decimal
            )
        }

        if (CalculationField.POST_CONCENTRATION in visibleFields) {
            TdmTextField(
                value = postDoseConcentration,
                onValueChange = onPostDoseConcentrationChange,
                label = "Post-dose concentration (mg/L)",
                keyboardType = KeyboardType.Decimal
            )
        }

        if (CalculationField.SAMPLING_TIME in visibleFields) {
            TdmTextField(
                value = samplingTime,
                onValueChange = onSamplingTimeChange,
                label = "Post sample — hours after infusion end",
                keyboardType = KeyboardType.Decimal,
                supportingText = "Time from the end of infusion to the post-dose blood sample."
            )
        }

        if (CalculationField.ADDITIONAL_TIMING in visibleFields) {
            TdmTextField(
                value = additionalTimingInformation,
                onValueChange = onAdditionalTimingInformationChange,
                label = "Pre/Post sample time difference (hours)",
                keyboardType = KeyboardType.Decimal,
                supportingText = "Elapsed clock-time difference between the pre and post sample timestamps."
            )
        }

        if (CalculationField.CREATININE_CLEARANCE in visibleFields) {
            TdmTextField(
                value = creatinineClearanceMlMin,
                onValueChange = onCreatinineClearanceMlMinChange,
                label = "Creatinine clearance (mL/min)",
                keyboardType = KeyboardType.Decimal
            )
        }

        if (CalculationField.INFUSION_DURATION in visibleFields) {
            TdmTextField(
                value = infusionDurationHours,
                onValueChange = onInfusionDurationHoursChange,
                label = "Infusion duration (hours)",
                keyboardType = KeyboardType.Decimal
            )
        }

        if (CalculationField.LAB_NOTE in visibleFields) {
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
                Text("Review")
            }
        }
    }
}
