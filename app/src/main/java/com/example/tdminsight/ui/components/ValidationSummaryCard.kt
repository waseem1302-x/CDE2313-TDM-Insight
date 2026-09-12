package com.example.tdminsight.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tdminsight.model.TdmInputKeys
import com.example.tdminsight.validation.ValidationIssue
import com.example.tdminsight.validation.ValidationSeverity

@Composable
fun ValidationSummaryCard(
    issues: List<ValidationIssue>,
    modifier: Modifier = Modifier
) {
    val errors = issues
        .filter { it.severity == ValidationSeverity.ERROR }
        .distinctBy { it.field to it.message }

    if (errors.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Check the highlighted information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            errors.forEach { issue ->
                Text(
                    text = "• ${fieldLabel(issue.field)}: ${issue.message}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

private fun fieldLabel(field: String): String = when (field) {
    TdmInputKeys.AGE_YEARS -> "Age"
    TdmInputKeys.BODY_WEIGHT_KG -> "Weight"
    "medicationDose" -> "Medication dose"
    "dosingInterval" -> "Dosing interval"
    "preDoseConcentration" -> "Pre-dose concentration"
    "postDoseConcentration" -> "Post-dose concentration"
    TdmInputKeys.POST_SAMPLE_DELAY_HOURS -> "Post-sample delay"
    TdmInputKeys.PRE_POST_SAMPLE_DIFFERENCE_HOURS -> "Pre/Post sample time difference"
    "creatinineClearanceMlMin" -> "Creatinine clearance"
    "infusionDurationHours" -> "Infusion duration"
    else -> field
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replaceFirstChar { character -> character.uppercase() }
}
