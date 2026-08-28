package com.univpm.fitquest.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.data.local.database.FitQuestDatabase
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.di.AppContainer
import com.univpm.fitquest.ui.theme.FitQuestTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryDetailDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var database: FitQuestDatabase
    private lateinit var repository: WorkoutRepository
    private lateinit var appContainer: AppContainer
    private var workoutId: Long = 0L

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FitQuestDatabase::class.java).build()
        appContainer = AppContainer(context, database)
        repository = appContainer.workoutRepository
        workoutId = runBlocking {
            repository.saveWorkout(
                workout = WorkoutEntity(
                    sport = "running",
                    startedAtMillis = 1_000L,
                    endedAtMillis = 61_000L,
                    durationMillis = 60_000L,
                    distanceMeters = 100.0,
                    isCompleted = true,
                ),
                routePoints = listOf(
                    routePoint(sequenceIndex = 0, latitude = 43.60, recordedAtMillis = 2_000L),
                    routePoint(sequenceIndex = 1, latitude = 43.61, recordedAtMillis = 3_000L),
                ),
            )
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun historyDetailDeleteRemovesWorkoutAndRoutePoints() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val running = context.getString(R.string.sport_running)
        val emptyHistory = context.getString(R.string.history_empty)

        composeRule.setContent {
            FitQuestTheme {
                FitQuestNavHost(
                    appContainer = appContainer,
                    startDestination = FitQuestDestination.History.route,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(running).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(running).assertIsDisplayed().performClick()
        composeRule.onNodeWithText(context.getString(R.string.workout_detail_title))
            .assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.workout_delete))
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.workout_delete_confirm))
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000L) {
            composeRule.onAllNodesWithText(emptyHistory).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(emptyHistory).assertIsDisplayed()

        runBlocking {
            assertTrue(repository.observeWorkouts().first().isEmpty())
            assertTrue(repository.observeRoutePoints(workoutId).first().isEmpty())
        }
    }

    private fun routePoint(
        sequenceIndex: Int,
        latitude: Double,
        recordedAtMillis: Long,
    ): RoutePointEntity {
        return RoutePointEntity(
            workoutId = 0L,
            sequenceIndex = sequenceIndex,
            latitude = latitude,
            longitude = 13.50,
            recordedAtMillis = recordedAtMillis,
        )
    }
}
