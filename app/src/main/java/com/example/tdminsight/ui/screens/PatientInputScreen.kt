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
import com.example.tdminsight.ui.components.TdmTextField

@Composable
fun PatientInputScreen(
    patientParameters: Map<String, String>,
    onParameterChange: (key: String, value: String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Patient / Case Details",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Enter fictional case information for the academic demonstration.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TdmTextField(
            value = patientParameters["caseId"].orEmpty(),
            onValueChange = { onParameterChange("caseId", it) },
            label = "Case ID"
        )
        TdmTextField(
            value = patientParameters["age"].orEmpty(),
            onValueChange = { onParameterChange("age", it) },
            label = "Age",
            keyboardType = KeyboardType.Number
        )
        TdmTextField(
            value = patientParameters["weight"].orEmpty(),
            onValueChange = { onParameterChange("weight", it) },
            label = "Weight",
            keyboardType = KeyboardType.Decimal,
            supportingText = "Use the unit defined by your approved project specification."
        )
        TdmTextField(
            value = patientParameters["notes"].orEmpty(),
            onValueChange = { onParameterChange("notes", it) },
            label = "Case notes (optional)",
            singleLine = false
        )

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
                Text("Next")
            }
        }
    }
}
