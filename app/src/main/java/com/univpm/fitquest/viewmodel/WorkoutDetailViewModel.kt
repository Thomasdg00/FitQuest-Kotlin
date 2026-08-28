package com.univpm.fitquest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutDetailUiState(
    val workout: WorkoutEntity? = null,
    val routePoints: List<RoutePointEntity> = emptyList(),
    val routeCharts: WorkoutRouteChartsUi = WorkoutRouteChartsUi(),
    val isDeleting: Boolean = false,
)

sealed interface WorkoutDetailEvent {
    data object WorkoutDeleted : WorkoutDetailEvent
    data object DeleteFailed : WorkoutDetailEvent
}

class WorkoutDetailViewModel(
    private val workoutId: Long,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val deleteState = MutableStateFlow(false)
    private val _events = MutableSharedFlow<WorkoutDetailEvent>()
    val events: SharedFlow<WorkoutDetailEvent> = _events.asSharedFlow()

    val uiState: StateFlow<WorkoutDetailUiState> = combine(
        workoutRepository.observeWorkout(workoutId),
        workoutRepository.observeRoutePoints(workoutId),
        deleteState,
    ) { workout, routePoints, isDeleting ->
        WorkoutDetailUiState(
            workout = workout,
            routePoints = routePoints,
            routeCharts = buildWorkoutRouteCharts(routePoints),
            isDeleting = isDeleting,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WorkoutDetailUiState(),
    )

    fun deleteWorkout() {
        if (deleteState.value) return

        viewModelScope.launch {
            deleteState.update { true }
            val deleted = runCatching {
                workoutRepository.deleteWorkout(workoutId)
            }.getOrDefault(false)
            deleteState.update { false }

            _events.emit(
                if (deleted) {
                    WorkoutDetailEvent.WorkoutDeleted
                } else {
                    WorkoutDetailEvent.DeleteFailed
                }
            )
        }
    }

    class Factory(
        private val workoutId: Long,
        private val workoutRepository: WorkoutRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WorkoutDetailViewModel(workoutId, workoutRepository) as T
        }
    }
}
