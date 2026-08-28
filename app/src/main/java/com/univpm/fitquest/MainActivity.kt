package com.univpm.fitquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.univpm.fitquest.ui.FitQuestApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = (application as FitQuestApplication).appContainer
        setContent {
            FitQuestApp(appContainer = appContainer)
        }
    }
}
