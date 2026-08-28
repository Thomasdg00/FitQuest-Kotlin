package com.univpm.fitquest.viewmodel

import android.location.Location
import com.univpm.fitquest.data.local.entity.RoutePointEntity

data class ChartPointUi(
    val x: Double,
    val y: Double,
)

data class WorkoutRouteChartsUi(
    val pacePoints: List<ChartPointUi> = emptyList(),
    val elevationPoints: List<ChartPointUi> = emptyList(),
)

fun buildWorkoutRouteCharts(routePoints: List<RoutePointEntity>): WorkoutRouteChartsUi {
    val sortedPoints = routePoints.sortedBy { it.sequenceIndex }
    if (sortedPoints.size < 2) return WorkoutRouteChartsUi()

    val firstTime = sortedPoints.first().recordedAtMillis
    val pacePoints = sortedPoints.mapNotNull { point ->
        val speed = point.speedMetersPerSecond?.toDouble() ?: return@mapNotNull null
        if (speed <= 0.1) return@mapNotNull null
        ChartPointUi(
            x = (point.recordedAtMillis - firstTime).coerceAtLeast(0L) / 60_000.0,
            y = 1_000.0 / speed / 60.0,
        )
    }

    var cumulativeDistanceMeters = 0.0
    val elevationPoints = mutableListOf<ChartPointUi>()
    val segmentDistanceMeters = FloatArray(1)
    sortedPoints.zipWithNext().forEach { (previous, current) ->
        Location.distanceBetween(
            previous.latitude,
            previous.longitude,
            current.latitude,
            current.longitude,
            segmentDistanceMeters,
        )
        cumulativeDistanceMeters += segmentDistanceMeters[0]
        current.altitudeMeters?.let { altitude ->
            elevationPoints += ChartPointUi(
                x = cumulativeDistanceMeters / 1_000.0,
                y = altitude,
            )
        }
    }

    return WorkoutRouteChartsUi(
        pacePoints = pacePoints,
        elevationPoints = elevationPoints,
    )
}
