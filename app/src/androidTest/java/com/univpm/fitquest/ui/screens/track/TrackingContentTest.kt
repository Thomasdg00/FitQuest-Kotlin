package com.univpm.fitquest.ui.screens.track

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.service.TrackingLifecycleState
import com.univpm.fitquest.ui.theme.FitQuestTheme
import com.univpm.fitquest.viewmodel.TrackPanelUiState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TrackingContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun idleTrackingShowsSportChoicesAndPermissionStartArea() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            FitQuestTheme {
                IdleTrackingView(
                    activeSport = Sport.Walking,
                    canStartTracking = false,
                    permissionState = TrackingPermissionState(
                        foregroundLocationGranted = false,
                        notificationPermissionRequired = true,
                        notificationPermissionGranted = false,
                    ),
                    onSportSelected = {},
                    onGrantLocation = {},
                    onGrantNotifications = {},
                    onStartTracking = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.sport_walking)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.sport_running)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.sport_cycling)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.track_permissions_required)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.track_grant_location)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.track_grant_notifications)).assertIsDisplayed()
    }

    @Test
    fun failedSaveShowsErrorAndRetryAction() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val errorMessage = context.getString(R.string.tracking_error_save_failed)
        var retryCount = 0

        composeRule.setContent {
            FitQuestTheme {
                ActiveTrackingView(
                    panelState = TrackPanelUiState(
                        lifecycleState = TrackingLifecycleState.SaveFailed,
                        sport = Sport.Running,
                        errorMessage = errorMessage,
                    ),
                    fallbackSport = Sport.Running,
                    elapsedMillis = MutableStateFlow(60_000L),
                    onPause = {},
                    onResume = {},
                    onStop = {},
                    onRetrySave = { retryCount++ },
                )
            }
        }

        composeRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.track_retry_save))
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.track_discard_workout))
            .assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(1, retryCount) }
    }

    @Test
    fun failedSaveDiscardRequiresConfirmationAndInvokesCallbackOnlyOnConfirm() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var discardCount = 0

        composeRule.setContent {
            FitQuestTheme {
                ActiveTrackingView(
                    panelState = TrackPanelUiState(
                        lifecycleState = TrackingLifecycleState.SaveFailed,
                        sport = Sport.Running,
                    ),
                    fallbackSport = Sport.Running,
                    elapsedMillis = MutableStateFlow(60_000L),
                    onPause = {},
                    onResume = {},
                    onStop = {},
                    onDiscardFailedSave = { discardCount++ },
                )
            }
        }

        val discardWorkout = context.getString(R.string.track_discard_workout)
        val dialogTitle = context.getString(R.string.track_discard_confirm_title)
        composeRule.onNodeWithText(discardWorkout).performClick()
        composeRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.track_discard_confirm_body))
            .assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.cancel)).performClick()
        assertEquals(0, composeRule.onAllNodesWithText(dialogTitle).fetchSemanticsNodes().size)
        composeRule.runOnIdle { assertEquals(0, discardCount) }

        composeRule.onNodeWithText(discardWorkout).performClick()
        composeRule.onNodeWithText(context.getString(R.string.track_discard_confirm)).performClick()
        composeRule.runOnIdle { assertEquals(1, discardCount) }
    }
}
