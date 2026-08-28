package com.univpm.fitquest.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.univpm.fitquest.data.local.entity.GoalEntity
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.WeatherRepository
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.WeatherForecastResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val weatherState: WeatherForecastResult = WeatherForecastResult.Loading,
    val locationName: String? = null,
    val lastWorkout: WorkoutEntity? = null,
    val lastWorkoutRoutePoints: List<RoutePointEntity> = emptyList(),
    val weeklyDistances: Map<Sport, Double> = Sport.entries.associateWith { 0.0 },
    val weeklyGoals: Map<Sport, Double> = Sport.entries.associateWith { 0.0 },
)

private fun buildHomeUiState(
    workouts: List<WorkoutEntity>,
    latestWorkout: WorkoutEntity?,
    routePoints: List<RoutePointEntity>,
    weatherState: WeatherForecastResult,
    locationName: String?,
    goals: List<GoalEntity>,
): HomeUiState {
    val weeklyProgressMeters = weeklyDistanceMetersBySport(workouts)

    val weeklyDistances = Sport.entries.associateWith { sport ->
        metersToKm(weeklyProgressMeters.getValue(sport))
    }

    val weeklyGoals = Sport.entries.associateWith { sport ->
        val goal = goals.firstOrNull { it.sport == sport.routeValue }
        metersToKm(goal?.weeklyDistanceGoalMeters ?: defaultWeeklyGoalMeters(sport))
    }

    return HomeUiState(
        weatherState = weatherState,
        locationName = locationName,
        lastWorkout = latestWorkout,
        lastWorkoutRoutePoints = routePoints,
        weeklyDistances = weeklyDistances,
        weeklyGoals = weeklyGoals,
    )
}

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val goalRepository: GoalRepository,
    private val weatherRepository: WeatherRepository,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val context: Context,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = workoutRepository.observeWorkouts()
        .flatMapLatest { workouts ->
            val latestWorkout = workouts.firstOrNull()
            val routeFlow: Flow<List<RoutePointEntity>> = if (latestWorkout != null) {
                workoutRepository.observeRoutePoints(latestWorkout.id)
            } else {
                flowOf(emptyList())
            }

            routeFlow.flatMapLatest { routePoints ->
                val forecastFlow = forecastForCurrentLocation()
                combine(
                    forecastFlow,
                    goalRepository.observeGoals(),
                ) { forecastData, goals ->
                    buildHomeUiState(
                        workouts = workouts,
                        latestWorkout = latestWorkout,
                        routePoints = routePoints,
                        weatherState = forecastData.first,
                        locationName = forecastData.second,
                        goals = goals,
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    @SuppressLint("MissingPermission")
    private fun forecastForCurrentLocation(): Flow<Pair<WeatherForecastResult, String?>> = callbackFlow {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    launch(Dispatchers.IO) {
                        val result = runCatching {
                            weatherRepository.fetchDailyForecast(location.latitude, location.longitude)
                        }.getOrDefault(WeatherForecastResult.Error)
                        val locName = runCatching {
                            val address = Geocoder(context, java.util.Locale.ITALIAN)
                                .getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                            address?.locality ?: address?.subAdminArea ?: address?.adminArea
                        }.getOrNull()
                        trySend(Pair(result, locName))
                        close()
                    }
                } else {
                    trySend(Pair(WeatherForecastResult.Error, null))
                    close()
                }
            }
            .addOnFailureListener {
                trySend(Pair(WeatherForecastResult.Error, null))
                close()
            }
        awaitClose { }
    }

    class Factory(
        private val workoutRepository: WorkoutRepository,
        private val goalRepository: GoalRepository,
        private val weatherRepository: WeatherRepository,
        private val fusedLocationClient: FusedLocationProviderClient,
        private val context: Context,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(workoutRepository, goalRepository, weatherRepository, fusedLocationClient, context) as T
        }
    }
}
