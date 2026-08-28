package com.univpm.fitquest.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.HomeUiState
import org.junit.Rule
import org.junit.Test

class HomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeShowsStatsWeeklyProgressAndEmptyLastWorkout() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FitQuestTheme {
                HomeContent(
                    uiState = HomeUiState(),
                    currentDate = "Oggi",
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.app_name)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.weekly_goal_progress)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_statistics)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.no_workouts_yet)).assertIsDisplayed()
    }
}
