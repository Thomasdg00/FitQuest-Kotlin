package com.univpm.fitquest.viewmodel

import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.location.PreviewLocation
import com.univpm.fitquest.tracking.service.InMemoryRoutePoint
import com.univpm.fitquest.tracking.service.TrackingLifecycleState
import com.univpm.fitquest.tracking.service.TrackingServiceState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackViewModelMappingTest {
    @Test
    fun panelUiStateKeepsCurrentAltitudeMeters() {
        val panelState = TrackingServiceState(
            lifecycleState = TrackingLifecycleState.Running,
            sport = Sport.Walking,
            currentAltitudeMeters = 123.4,
        ).toPanelUiState()

        assertEquals(123.4, panelState.currentAltitudeMeters ?: 0.0, 0.0)
    }

    @Test
    fun mapUiStateCanCarryPreviewLocationWithoutAddingRoutePoints() {
        val preview = PreviewLocation(latitude = 43.6170, longitude = 13.5190)

        val mapState = TrackingServiceState(
            lifecycleState = TrackingLifecycleState.Idle,
            distanceMeters = 0.0,
            routePoints = emptyList(),
        ).toMapUiState(preview)

        assertEquals(preview, mapState.previewLocation)
        assertTrue(mapState.routePoints.isEmpty())
        assertEquals(TrackingLifecycleState.Idle, mapState.lifecycleState)
    }

    @Test
    fun mapUiStateKeepsTrackingRouteSeparateFromPreviewLocation() {
        val routePoint = InMemoryRoutePoint(
            latitude = 43.6180,
            longitude = 13.5200,
            recordedAtMillis = 1_000L,
            altitudeMeters = null,
            accuracyMeters = null,
            speedMetersPerSecond = null,
        )
        val preview = PreviewLocation(latitude = 43.6170, longitude = 13.5190)

        val mapState = TrackingServiceState(
            lifecycleState = TrackingLifecycleState.Running,
            distanceMeters = 12.0,
            routePoints = listOf(routePoint),
        ).toMapUiState(preview)

        assertEquals(preview, mapState.previewLocation)
        assertEquals(listOf(routePoint), mapState.routePoints)
        assertEquals(TrackingLifecycleState.Running, mapState.lifecycleState)
    }

    @Test
    fun panelUiStateExposesRecoverableSaveFailure() {
        val errorMessage = "save failed"

        val panelState = TrackingServiceState(
            lifecycleState = TrackingLifecycleState.SaveFailed,
            sport = Sport.Running,
            errorMessage = errorMessage,
        ).toPanelUiState()

        assertEquals(TrackingLifecycleState.SaveFailed, panelState.lifecycleState)
        assertEquals(errorMessage, panelState.errorMessage)
    }
}
