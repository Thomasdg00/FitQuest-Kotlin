package com.univpm.fitquest.ui.screens.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.HistoryUiState
import org.junit.Rule
import org.junit.Test

class HistoryContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun historyShowsTitleSubtitleAndEmptyState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FitQuestTheme {
                HistoryContent(
                    uiState = HistoryUiState(),
                    onOpenWorkout = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.history_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.history_subtitle)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.history_empty)).assertIsDisplayed()
    }
}
