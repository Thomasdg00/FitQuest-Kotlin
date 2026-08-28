package com.univpm.fitquest.ui.screens.track

import com.univpm.fitquest.tracking.location.PreviewLocation
import com.univpm.fitquest.tracking.service.InMemoryRoutePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveRouteMapCameraTest {
    @Test
    fun initialCameraTargetUsesPreviewLocationBeforeFallback() {
        val target = initialRouteMapCameraTarget(
            routePoints = emptyList(),
            previewLocation = PreviewLocation(latitude = 45.4642, longitude = 9.1900),
        )

        assertEquals(45.4642, target.latitude, 0.0)
        assertEquals(9.1900, target.longitude, 0.0)
        assertEquals(16f, target.zoom, 0.0f)
    }

    @Test
    fun initialCameraTargetFallsBackOnlyWithoutPreviewOrRoutePoint() {
        val target = initialRouteMapCameraTarget(
            routePoints = emptyList(),
            previewLocation = null,
        )

        assertEquals(43.6168, target.latitude, 0.0)
        assertEquals(13.5189, target.longitude, 0.0)
        assertEquals(12f, target.zoom, 0.0f)
    }

    @Test
    fun previewCameraCenteringRunsOnlyOnceBeforeRoutePointsExist() {
        val previewLocation = PreviewLocation(latitude = 45.4642, longitude = 9.1900)
        val routePoint = InMemoryRoutePoint(
            latitude = 45.4650,
            longitude = 9.1910,
            recordedAtMillis = 1_000L,
            altitudeMeters = null,
            accuracyMeters = null,
            speedMetersPerSecond = null,
        )

        assertTrue(
            shouldCenterOnPreviewLocation(
                hasCenteredOnUser = false,
                routePoints = emptyList(),
                previewLocation = previewLocation,
            )
        )
        assertFalse(
            shouldCenterOnPreviewLocation(
                hasCenteredOnUser = true,
                routePoints = emptyList(),
                previewLocation = previewLocation,
            )
        )
        assertFalse(
            shouldCenterOnPreviewLocation(
                hasCenteredOnUser = false,
                routePoints = listOf(routePoint),
                previewLocation = previewLocation,
            )
        )
    }
}
