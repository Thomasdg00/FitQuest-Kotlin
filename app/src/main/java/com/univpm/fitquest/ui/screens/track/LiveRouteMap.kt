package com.univpm.fitquest.ui.screens.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.univpm.fitquest.tracking.location.PreviewLocation
import com.univpm.fitquest.tracking.service.InMemoryRoutePoint
import com.univpm.fitquest.tracking.service.TrackingLifecycleState

private val fallbackMapCameraTarget = RouteMapCameraTarget(
    latitude = 43.6168,
    longitude = 13.5189,
    zoom = 12f,
)

internal data class RouteMapCameraTarget(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float,
) {
    fun toLatLng(): LatLng = LatLng(latitude, longitude)
}

internal fun initialRouteMapCameraTarget(
    routePoints: List<InMemoryRoutePoint>,
    previewLocation: PreviewLocation?,
): RouteMapCameraTarget {
    val latestRoutePoint = routePoints.lastOrNull()
    return when {
        latestRoutePoint != null -> RouteMapCameraTarget(
            latitude = latestRoutePoint.latitude,
            longitude = latestRoutePoint.longitude,
            zoom = 16f,
        )
        previewLocation != null -> RouteMapCameraTarget(
            latitude = previewLocation.latitude,
            longitude = previewLocation.longitude,
            zoom = 16f,
        )
        else -> fallbackMapCameraTarget
    }
}

internal fun shouldCenterOnPreviewLocation(
    hasCenteredOnUser: Boolean,
    routePoints: List<InMemoryRoutePoint>,
    previewLocation: PreviewLocation?,
): Boolean {
    return !hasCenteredOnUser && routePoints.isEmpty() && previewLocation != null
}

@Composable
fun LiveRouteMap(
    routePoints: List<InMemoryRoutePoint>,
    lifecycleState: TrackingLifecycleState,
    previewLocation: PreviewLocation? = null,
    locationPermissionGranted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val points: List<LatLng> = remember(routePoints) {
        routePoints.map { LatLng(it.latitude, it.longitude) }
    }
    val latestPoint: LatLng? = points.lastOrNull()
    val previewPoint: LatLng? = remember(previewLocation) {
        previewLocation?.let { LatLng(it.latitude, it.longitude) }
    }
    val visibleUserPoint = latestPoint ?: previewPoint
    val initialCameraTarget = remember {
        initialRouteMapCameraTarget(routePoints, previewLocation)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            initialCameraTarget.toLatLng(),
            initialCameraTarget.zoom,
        )
    }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        )
    }
    val mapProperties = remember(locationPermissionGranted) {
        MapProperties(isMyLocationEnabled = locationPermissionGranted)
    }
    var hasCenteredOnUser by remember { mutableStateOf(initialCameraTarget != fallbackMapCameraTarget) }

    LaunchedEffect(previewLocation, routePoints) {
        if (shouldCenterOnPreviewLocation(hasCenteredOnUser, routePoints, previewLocation)) {
            val target = initialRouteMapCameraTarget(routePoints, previewLocation)
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target.toLatLng(), target.zoom))
            hasCenteredOnUser = true
        }
    }

    Box(
        modifier = modifier,
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
        ) {
            if (points.size >= 2) {
                Polyline(
                    points = points,
                    color = Color(0xFF1D4ED8),
                    width = 15f
                )
            }
            visibleUserPoint?.let { point: LatLng ->
                Circle(
                    center = point,
                    fillColor = Color(0xFF16A34A),
                    radius = 7.0,
                    strokeColor = Color.White,
                    strokeWidth = 4f
                )
            }
        }
    }
}
