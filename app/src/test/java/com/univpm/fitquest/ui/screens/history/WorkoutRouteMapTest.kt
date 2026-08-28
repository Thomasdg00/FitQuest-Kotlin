package com.univpm.fitquest.ui.screens.history

import com.univpm.fitquest.data.local.entity.RoutePointEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutRouteMapTest {
    @Test
    fun savedRoutePointsAreSortedAndConvertedToGoogleMapsLatLng() {
        val points = listOf(
            routePoint(sequenceIndex = 2, latitude = 43.6200, longitude = 13.5200),
            routePoint(sequenceIndex = 1, latitude = 43.6100, longitude = 13.5100),
        )

        val mapPoints = points.toSavedRouteMapPoints()

        assertEquals(13.5100, mapPoints[0].longitude, 0.0)
        assertEquals(43.6100, mapPoints[0].latitude, 0.0)
        assertEquals(13.5200, mapPoints[1].longitude, 0.0)
        assertEquals(43.6200, mapPoints[1].latitude, 0.0)
    }

    private fun routePoint(
        sequenceIndex: Int,
        latitude: Double,
        longitude: Double,
    ) = RoutePointEntity(
        workoutId = 1,
        sequenceIndex = sequenceIndex,
        latitude = latitude,
        longitude = longitude,
        recordedAtMillis = sequenceIndex.toLong(),
    )
}
