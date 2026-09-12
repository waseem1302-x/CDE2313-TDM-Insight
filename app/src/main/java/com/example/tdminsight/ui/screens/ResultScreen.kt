package com.example.tdminsight.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tdminsight.model.TdmResult
import com.example.tdminsight.ui.components.ResultCard

@Composable
fun ResultScreen(
    result: TdmResult,
    onBackToReview: () -> Unit,
    onOpenDisclaimer: () -> Unit,
    onStartNewCase: () -> Unit,
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
            text = "TDM Results",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Academic prototype output. Values shown here must come from the approved calculation engine; this screen does not calculate or invent clinical results.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ResultCard(
            title = "Intermediate Values",
            values = result.intermediateValues
        )
        ResultCard(
            title = "Pharmacokinetic Parameters",
            values = result.pharmacokineticParameters
        )
        ResultCard(
            title = "Final Values",
            values = result.finalValues
        )

        Text(
            text = "Calculation Explanation",
            style = MaterialTheme.typography.titleLarge
        )

        if (result.explanation.isEmpty()) {
            Text(
                text = "No calculation explanation is available yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            result.explanation.forEachIndexed { index, step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${index + 1}. ${step.title}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = step.detail,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackToReview,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Review")
            }
            OutlinedButton(
                onClick = onStartNewCase,
                modifier = Modifier.weight(1f)
            ) {
                Text("New Case")
            }
        }

        TextButton(
            onClick = onOpenDisclaimer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Academic Disclaimer")
        }
    }
}
