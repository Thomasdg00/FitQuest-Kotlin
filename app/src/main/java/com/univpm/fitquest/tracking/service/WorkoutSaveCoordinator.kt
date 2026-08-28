package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.domain.model.Sport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class WorkoutSaveCoordinator(
    private val workoutRepository: WorkoutRepository,
) {
    suspend fun saveCompletedWorkout(request: CompletedWorkoutSaveRequest) {
        withContext(Dispatchers.IO) {
            val workout = buildTrackedWorkoutEntity(
                sport = request.sport,
                startedAtMillis = request.startedAtMillis,
                endedAtMillis = request.endedAtMillis,
                durationMillis = request.durationMillis,
                distanceMeters = request.distanceMeters,
                caloriesKcal = request.caloriesKcal,
                elevationGainMeters = request.elevationGainMeters,
                elevationLossMeters = request.elevationLossMeters,
            )
            val routeEntities = request.routeSnapshot.toRoutePointEntities(workoutId = 0L)
            workoutRepository.saveWorkout(workout, routeEntities)
        }
    }
}

internal data class CompletedWorkoutSaveRequest(
    val sport: Sport,
    val startedAtMillis: Long,
    val endedAtMillis: Long,
    val durationMillis: Long,
    val distanceMeters: Double,
    val routeSnapshot: List<InMemoryRoutePoint>,
    val caloriesKcal: Double,
    val elevationGainMeters: Double,
    val elevationLossMeters: Double,
)
