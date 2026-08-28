package com.univpm.fitquest.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.univpm.fitquest.R
import com.univpm.fitquest.di.AppContainer
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.ui.screens.history.HistoryScreen
import com.univpm.fitquest.ui.screens.history.WorkoutDetailScreen
import com.univpm.fitquest.ui.screens.home.HomeScreen
import com.univpm.fitquest.ui.screens.settings.SettingsScreen
import com.univpm.fitquest.ui.screens.stats.StatsScreen
import com.univpm.fitquest.ui.screens.track.TrackScreen
import com.univpm.fitquest.viewmodel.HistoryViewModel
import com.univpm.fitquest.viewmodel.HomeViewModel
import com.univpm.fitquest.viewmodel.SettingsViewModel
import com.univpm.fitquest.viewmodel.StatsViewModel
import com.univpm.fitquest.viewmodel.TrackViewModel
import com.univpm.fitquest.viewmodel.WorkoutDetailViewModel

@Composable
fun FitQuestNavHost(
    appContainer: AppContainer,
    startDestination: String = FitQuestDestination.Home.route,
) {
    val navController = rememberNavController()
    val bottomDestinations = listOf(
        BottomDestination(FitQuestDestination.Home, R.string.nav_home, Icons.Outlined.Home),
        BottomDestination(FitQuestDestination.Track, R.string.nav_tracking, Icons.Outlined.PlayArrow),
        BottomDestination(FitQuestDestination.History, R.string.nav_history, Icons.Outlined.BarChart),
        BottomDestination(FitQuestDestination.Settings, R.string.nav_settings, Icons.Outlined.Settings),
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomNavigation = currentDestination?.hierarchy?.any {
        shouldShowBottomNavigation(it.route)
    } == true

    Scaffold(
        bottomBar = {
            if (showBottomNavigation) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        val label = stringResource(destination.labelRes)
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == destination.destination.route
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(FitQuestDestination.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        workoutRepository = appContainer.workoutRepository,
                        goalRepository = appContainer.goalRepository,
                        weatherRepository = appContainer.weatherRepository,
                        fusedLocationClient = appContainer.fusedLocationClient,
                        context = appContainer.context,
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenStats = { navController.navigate(FitQuestDestination.Stats.route) },
                )
            }
            composable(FitQuestDestination.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(appContainer.workoutRepository)
                )
                HistoryScreen(
                    viewModel = historyViewModel,
                    onOpenWorkout = { workoutId ->
                        navController.navigate(FitQuestDestination.WorkoutDetail.routeFor(workoutId))
                    },
                )
            }
            composable(FitQuestDestination.Stats.route) {
                val statsViewModel: StatsViewModel = viewModel(
                    factory = StatsViewModel.Factory(
                        workoutRepository = appContainer.workoutRepository,
                    )
                )
                StatsScreen(
                    viewModel = statsViewModel,
                    onNavigateUp = { navController.navigateUp() },
                )
            }
            composable(
                route = FitQuestDestination.WorkoutDetail.route,
                arguments = listOf(
                    navArgument(FitQuestDestination.WorkoutDetail.WORKOUT_ID_ARG) {
                        type = NavType.LongType
                    }
                ),
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong(
                    FitQuestDestination.WorkoutDetail.WORKOUT_ID_ARG
                ) ?: 0L
                val workoutDetailViewModel: WorkoutDetailViewModel = viewModel(
                    factory = WorkoutDetailViewModel.Factory(
                        workoutId = workoutId,
                        workoutRepository = appContainer.workoutRepository,
                    )
                )
                WorkoutDetailScreen(
                    viewModel = workoutDetailViewModel,
                    onBack = { navController.navigateUp() },
                    onDeleted = {
                        navController.popBackStack(
                            route = FitQuestDestination.History.route,
                            inclusive = false,
                        )
                    },
                )
            }
            composable(FitQuestDestination.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        userSettingsRepository = appContainer.userSettingsRepository,
                        goalRepository = appContainer.goalRepository,
                    )
                )
                SettingsScreen(viewModel = settingsViewModel)
            }
            composable(FitQuestDestination.Track.route) {
                val trackViewModel: TrackViewModel = viewModel(
                    factory = TrackViewModel.Factory()
                )
                TrackScreen(
                    initialSport = Sport.Walking,
                    viewModel = trackViewModel,
                    previewLocationProvider = appContainer.previewLocationProvider,
                )
            }
        }
    }
}

private data class BottomDestination(
    val destination: FitQuestDestination,
    @param:StringRes val labelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
