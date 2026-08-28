package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val weekly: PeriodStatsUi = PeriodStatsUi(),
    val monthly: PeriodStatsUi = PeriodStatsUi(),
    val sportBreakdown: List<SportBreakdownUi> = Sport.entries.map { SportBreakdownUi(sport = it) },
    val monthlyTrend: List<MonthlyTrendSegmentUi> = emptyList(),
) {
    val hasWorkouts: Boolean
        get() = monthly.workoutCount > 0 || weekly.workoutCount > 0
}

data class PeriodStatsUi(
    val totalDistanceKm: Double = 0.0,
    val totalDurationMinutes: Long = 0L,
    val workoutCount: Int = 0,
    val totalCaloriesKcal: Double = 0.0,
)

data class SportBreakdownUi(
    val sport: Sport,
    val weeklyDistanceKm: Double = 0.0,
)

data class MonthlyTrendSegmentUi(
    val label: String,
    val distanceKm: Double,
)

class StatsViewModel(workoutRepository: WorkoutRepository) : ViewModel() {
    val uiState: StateFlow<StatsUiState> = workoutRepository.observeWorkouts()
        .map { workouts -> workouts.toStatsUiState() }
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    class Factory(
        private val workoutRepository: WorkoutRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatsViewModel(workoutRepository) as T
        }
    }
}

private fun List<WorkoutEntity>.toStatsUiState(): StatsUiState {
    val completed = filter { it.isCompleted }
    val now = System.currentTimeMillis()
    val weekStart = startOfWeekMillis(now)
    val monthStart = startOfMonthMillis(now)
    val weeklyWorkouts = completed.filter { it.startedAtMillis in weekStart..now }
    val monthlyWorkouts = completed.filter { it.startedAtMillis in monthStart..now }

    return StatsUiState(
        weekly = weeklyWorkouts.toPeriodStats(),
        monthly = monthlyWorkouts.toPeriodStats(),
        sportBreakdown = Sport.entries.map { sport ->
            SportBreakdownUi(
                sport = sport,
                weeklyDistanceKm = weeklyWorkouts.distanceKmForSport(sport),
            )
        },
        monthlyTrend = monthlyDistanceTrend(this, now),
    )
}

internal fun monthlyDistanceTrend(
    workouts: List<WorkoutEntity>,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<MonthlyTrendSegmentUi> {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
    val currentMonth = YearMonth.from(now)
    val totalsMeters = DoubleArray(((now.dayOfMonth - 1) / 7) + 1)

    workouts.asSequence()
        .filter { it.isCompleted && it.startedAtMillis <= nowMillis }
        .map { it to Instant.ofEpochMilli(it.startedAtMillis).atZone(zoneId).toLocalDate() }
        .filter { (_, date) -> YearMonth.from(date) == currentMonth }
        .forEach { (workout, date) -> totalsMeters[(date.dayOfMonth - 1) / 7] += workout.distanceMeters }

    return totalsMeters.mapIndexed { index, distanceMeters ->
        val startDay = index * 7 + 1
        MonthlyTrendSegmentUi(
            label = "$startDay–${minOf(startDay + 6, currentMonth.lengthOfMonth())}",
            distanceKm = distanceMeters / 1_000.0,
        )
    }
}

private fun List<WorkoutEntity>.toPeriodStats(): PeriodStatsUi {
    return PeriodStatsUi(
        totalDistanceKm = sumOf { it.distanceMeters } / 1_000.0,
        totalDurationMinutes = sumOf { it.durationMillis } / 60_000L,
        workoutCount = size,
        totalCaloriesKcal = sumOf { it.caloriesKcal },
    )
}

private fun List<WorkoutEntity>.distanceKmForSport(sport: Sport): Double {
    return filter { it.sport == sport.routeValue }.sumOf { it.distanceMeters } / 1_000.0
}

private fun startOfWeekMillis(nowMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun startOfMonthMillis(nowMillis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
