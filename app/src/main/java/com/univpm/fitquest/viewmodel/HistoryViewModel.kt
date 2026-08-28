package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val workouts: List<WorkoutEntity> = emptyList()
)

class HistoryViewModel(
    workoutRepository: WorkoutRepository
) : ViewModel() {
    val uiState: StateFlow<HistoryUiState> = workoutRepository.observeWorkouts()
        .map { workouts -> HistoryUiState(workouts = workouts) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    class Factory(
        private val workoutRepository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(workoutRepository) as T
        }
    }
}
