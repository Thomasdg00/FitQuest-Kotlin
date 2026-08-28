package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.domain.model.Sport

enum class TrackingLifecycleState {
    Idle,
    Running,
    Paused,
    Stopping,
    SaveFailed,
}

data class TrackingServiceState(
    val lifecycleState: TrackingLifecycleState = TrackingLifecycleState.Idle,
    val sport: Sport? = null,
    val elapsedMillis: Long = 0L,
    val distanceMeters: Double = 0.0,
    val latestLatitude: Double? = null,
    val latestLongitude: Double? = null,
    val currentSpeedMetersPerSecond: Float? = null,
    val currentAltitudeMeters: Double? = null,
    val routePoints: List<InMemoryRoutePoint> = emptyList(),
    val estimatedCaloriesKcal: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val errorMessage: String? = null,
)


data class InMemoryRoutePoint(
    val latitude: Double,
    val longitude: Double,
    val recordedAtMillis: Long,
    val altitudeMeters: Double?,
    val accuracyMeters: Float?,
    val speedMetersPerSecond: Float?,
)
