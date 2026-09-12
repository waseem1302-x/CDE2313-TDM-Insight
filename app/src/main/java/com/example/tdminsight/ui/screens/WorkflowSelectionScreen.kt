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
import androidx.compose.ui.unit.dp
import com.example.tdminsight.model.WorkflowType
import com.example.tdminsight.ui.components.WorkflowCard

@Composable
fun WorkflowSelectionScreen(
    selectedWorkflow: WorkflowType?,
    onWorkflowSelected: (WorkflowType) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
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
            text = "Select Vancomycin Workflow",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Choose the workflow that matches the fictional case. The next screen only shows fields required for that workflow.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        WorkflowType.entries.forEach { workflow ->
            WorkflowCard(
                workflow = workflow,
                selected = workflow == selectedWorkflow,
                onClick = { onWorkflowSelected(workflow) }
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
                onClick = onContinue,
                enabled = selectedWorkflow != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
    }
}
