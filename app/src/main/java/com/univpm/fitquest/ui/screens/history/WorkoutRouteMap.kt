package com.univpm.fitquest.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.univpm.fitquest.R
import com.univpm.fitquest.data.local.entity.RoutePointEntity

@Composable
fun WorkoutRouteMap(
    routePoints: List<RoutePointEntity>,
    modifier: Modifier = Modifier,
    mapHeight: androidx.compose.ui.unit.Dp = 240.dp,
    showContainer: Boolean = true,
    shape: Shape? = null,
) {
    val points: List<LatLng> = remember(routePoints) { routePoints.toSavedRouteMapPoints() }
    val resolvedShape: Shape = shape ?: MaterialTheme.shapes.large

    if (showContainer) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = resolvedShape,
        ) {
            WorkoutRouteMapContent(
                points = points,
                mapHeight = mapHeight,
                shape = resolvedShape,
            )
        }
    } else {
        WorkoutRouteMapContent(
            points = points,
            mapHeight = mapHeight,
            shape = resolvedShape,
            modifier = modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WorkoutRouteMapContent(
    points: List<LatLng>,
    mapHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    shape: Shape,
) {
    Box(modifier = modifier.clip(shape)) {
        if (points.isEmpty()) {
            EmptySavedRoute()
        } else {
            SavedRouteMap(points = points, mapHeight = mapHeight)
        }
    }
}

@Composable
private fun SavedRouteMap(
    points: List<LatLng>,
    mapHeight: androidx.compose.ui.unit.Dp,
) {
    val routeColor = MaterialTheme.colorScheme.primary
    val startColor = MaterialTheme.colorScheme.secondary
    val endColor = MaterialTheme.colorScheme.tertiary
    val markerStrokeColor = MaterialTheme.colorScheme.surface
    
    val initialPosition: LatLng = points.first()
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 15.5f)
    }
    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        )
    }

    LaunchedEffect(points) {
        if (points.size == 1) {
            val singlePoint: LatLng = points.first()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(singlePoint, 15.5f))
        } else if (points.isNotEmpty()) {
            val bounds = LatLngBounds.builder().apply {
                points.forEach { point: LatLng -> include(point) }
            }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 64))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(mapHeight),
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
        ) {
            if (points.size >= 2) {
                Polyline(
                    points = points,
                    color = routeColor,
                    width = 15f
                )
            }
            Circle(
                center = points.first(),
                fillColor = startColor,
                radius = 6.0,
                strokeColor = markerStrokeColor,
                strokeWidth = 4f
            )
            Circle(
                center = points.last(),
                fillColor = endColor,
                radius = 7.0,
                strokeColor = markerStrokeColor,
                strokeWidth = 4f
            )
        }
    }
}

@Composable
private fun EmptySavedRoute() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.route),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.route_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun List<RoutePointEntity>.toSavedRouteMapPoints(): List<LatLng> {
    return sortedBy { it.sequenceIndex }
        .map { routePoint -> LatLng(routePoint.latitude, routePoint.longitude) }
}
