package com.univpm.fitquest.navigation

import com.univpm.fitquest.ui.navigation.FitQuestDestination
import com.univpm.fitquest.ui.navigation.shouldShowBottomNavigation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FitQuestDestinationTest {
    @Test
    fun homeRouteIsHome() {
        assertEquals("home", FitQuestDestination.Home.route)
    }

    @Test
    fun trackRouteIsDirectRecordingRoute() {
        assertEquals("track", FitQuestDestination.Track.route)
    }

    @Test
    fun statsRouteIsStats() {
        assertEquals("stats", FitQuestDestination.Stats.route)
    }

    @Test
    fun workoutDetailRouteIncludesWorkoutId() {
        assertEquals("workout-detail/42", FitQuestDestination.WorkoutDetail.routeFor(42L))
    }

    @Test
    fun bottomNavigationIsLimitedToTopLevelDestinations() {
        listOf(
            FitQuestDestination.Home.route,
            FitQuestDestination.Track.route,
            FitQuestDestination.History.route,
            FitQuestDestination.Settings.route,
        ).forEach { route ->
            assertTrue(shouldShowBottomNavigation(route))
        }

        listOf(
            FitQuestDestination.Stats.route,
            FitQuestDestination.WorkoutDetail.route,
            FitQuestDestination.WorkoutDetail.routeFor(42L),
            null,
        ).forEach { route ->
            assertFalse(shouldShowBottomNavigation(route))
        }
    }
}
