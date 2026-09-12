package com.example.tdminsight.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DisclaimerScreen(
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
            text = "Academic Disclaimer",
            style = MaterialTheme.typography.headlineSmall
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TDM Insight is an academic prototype created for educational and software-development purposes only.",
                    style = MaterialTheme.typography.bodyLarge
                )
                DisclaimerPoint("It is not clinically validated.")
                DisclaimerPoint("It is not prescribing software.")
                DisclaimerPoint("It is not diagnostic software.")
                DisclaimerPoint("It is not autonomous treatment-decision software.")
                DisclaimerPoint("Use fictional demonstration cases only.")
                DisclaimerPoint("Clinical equations, targets, units, assumptions and reference values require lecturer-approved authoritative sources before implementation or use.")
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun DisclaimerPoint(text: String) {
    Text(
        text = "• $text",
        style = MaterialTheme.typography.bodyMedium
    )
}
