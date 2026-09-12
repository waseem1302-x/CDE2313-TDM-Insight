package com.example.tdminsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tdminsight.ui.TdmApp
import com.example.tdminsight.ui.theme.TDMInsightTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TDMInsightTheme {
                TdmApp()
            }
        }
    }
}
