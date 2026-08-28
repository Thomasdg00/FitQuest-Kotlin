package com.univpm.fitquest.ui.navigation

sealed class FitQuestDestination(val route: String) {
    data object Home : FitQuestDestination("home")
    data object History : FitQuestDestination("history")
    data object Stats : FitQuestDestination("stats")
    data object WorkoutDetail : FitQuestDestination("workout-detail/{workoutId}") {
        const val WORKOUT_ID_ARG = "workoutId"

        fun routeFor(workoutId: Long): String = "workout-detail/$workoutId"
    }
    data object Settings : FitQuestDestination("settings")
    data object Track : FitQuestDestination("track")
}

private val bottomNavigationRoutes = setOf(
    FitQuestDestination.Home.route,
    FitQuestDestination.Track.route,
    FitQuestDestination.History.route,
    FitQuestDestination.Settings.route,
)

internal fun shouldShowBottomNavigation(route: String?): Boolean {
    return route in bottomNavigationRoutes
}
