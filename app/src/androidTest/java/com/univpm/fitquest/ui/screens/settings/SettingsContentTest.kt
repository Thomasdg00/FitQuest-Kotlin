package com.univpm.fitquest.ui.screens.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.SettingsUiState
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsShowsWeightThemeDropdownAndGoalsOnly() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.setContent {
            FitQuestTheme {
                SettingsContent(
                    uiState = SettingsUiState(),
                    onSaveBodyWeight = {},
                    onThemeSelected = {},
                    onSaveWeeklyGoals = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.body_weight)).assertIsDisplayed()
        composeRule.onNodeWithText("70,0").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.appearance)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.theme_light)).performScrollTo().performClick()
        composeRule.onNodeWithText(context.getString(R.string.theme_dark)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.weekly_goals)).performScrollTo().assertIsDisplayed()
        listOf("Altezza", "Età", "Sesso", "Lingua", "Predefinito di sistema").forEach { removedText ->
            assertEquals(0, composeRule.onAllNodesWithText(removedText).fetchSemanticsNodes().size)
        }
    }
}
