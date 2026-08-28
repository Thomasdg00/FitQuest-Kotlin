package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.GoalEntity
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.UserSettingsRepository
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val bodyWeightKg: Double = 70.0,
    val themeMode: ThemeMode = ThemeMode.Light,
    val weeklyGoalKmBySport: Map<Sport, Double> = Sport.entries.associateWith {
        metersToKm(defaultWeeklyGoalMeters(it))
    },
)

class SettingsViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val goalRepository: GoalRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        userSettingsRepository.observeSettings(),
        goalRepository.observeGoals(),
    ) { settings, goals ->
            val resolved = settings ?: UserSettingsEntity()
            SettingsUiState(
                bodyWeightKg = resolved.bodyWeightKg,
                themeMode = ThemeMode.fromStorageValue(resolved.themeMode),
                weeklyGoalKmBySport = Sport.entries.associateWith { sport ->
                    val goal = goals.firstOrNull { it.sport == sport.routeValue }
                    metersToKm(goal?.weeklyDistanceGoalMeters ?: defaultWeeklyGoalMeters(sport))
                },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    init {
        viewModelScope.launch {
            userSettingsRepository.ensureSettings()
            Sport.entries.forEach { sport ->
                if (goalRepository.getGoalForSport(sport.routeValue) == null) {
                    goalRepository.saveGoal(
                        GoalEntity(
                            sport = sport.routeValue,
                            weeklyDistanceGoalMeters = defaultWeeklyGoalMeters(sport),
                            updatedAtMillis = System.currentTimeMillis(),
                        )
                    )
                }
            }
        }
    }

    fun saveBodyWeight(weightKg: Double) {
        viewModelScope.launch {
            userSettingsRepository.updateBodyWeight(
                weightKg.takeIf { it.isFinite() && it in 20.0..300.0 } ?: 70.0,
            )
        }
    }

    fun saveThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            userSettingsRepository.updateThemeMode(themeMode)
        }
    }

    fun saveWeeklyGoals(goalKmBySport: Map<Sport, Double>) {
        viewModelScope.launch {
            goalKmBySport.forEach { (sport, targetKm) ->
                val existing = goalRepository.getGoalForSport(sport.routeValue)
                goalRepository.saveGoal(
                    GoalEntity(
                        id = existing?.id ?: 0,
                        sport = sport.routeValue,
                        weeklyDistanceGoalMeters = kmToMeters(targetKm.coerceAtLeast(0.0)),
                        updatedAtMillis = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    class Factory(
        private val userSettingsRepository: UserSettingsRepository,
        private val goalRepository: GoalRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userSettingsRepository, goalRepository) as T
        }
    }
}
