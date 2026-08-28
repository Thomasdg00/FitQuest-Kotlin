package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import com.univpm.fitquest.domain.model.Sport

fun buildTrackedWorkoutEntity(
    sport: Sport,
    startedAtMillis: Long,
    endedAtMillis: Long,
    durationMillis: Long,
    distanceMeters: Double,
    caloriesKcal: Double = 0.0,
    elevationGainMeters: Double = 0.0,
    elevationLossMeters: Double = 0.0,
): WorkoutEntity {
    val safeDurationMillis = durationMillis.coerceAtLeast(0L)
    val safeDistanceMeters = distanceMeters.coerceAtLeast(0.0)
    val durationSeconds = safeDurationMillis / 1_000.0

    return WorkoutEntity(
        sport = sport.routeValue,
        startedAtMillis = startedAtMillis,
        endedAtMillis = endedAtMillis,
        durationMillis = safeDurationMillis,
        distanceMeters = safeDistanceMeters,
        averageSpeedMetersPerSecond = if (durationSeconds > 0.0) {
            safeDistanceMeters / durationSeconds
        } else {
            0.0
        },
        caloriesKcal = caloriesKcal.coerceAtLeast(0.0),
        elevationGainMeters = elevationGainMeters.coerceAtLeast(0.0),
        elevationLossMeters = elevationLossMeters.coerceAtLeast(0.0),
        isCompleted = true,
    )
}

fun List<InMemoryRoutePoint>.toRoutePointEntities(workoutId: Long): List<RoutePointEntity> {
    return mapIndexed { index, point ->
        RoutePointEntity(
            workoutId = workoutId,
            sequenceIndex = index,
            latitude = point.latitude,
            longitude = point.longitude,
            recordedAtMillis = point.recordedAtMillis,
            altitudeMeters = point.altitudeMeters,
            accuracyMeters = point.accuracyMeters,
            speedMetersPerSecond = point.speedMetersPerSecond,
        )
    }
}
