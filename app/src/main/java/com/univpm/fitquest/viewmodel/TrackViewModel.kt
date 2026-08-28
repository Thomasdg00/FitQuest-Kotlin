package com.univpm.fitquest.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.location.PreviewLocation
import com.univpm.fitquest.tracking.service.InMemoryRoutePoint
import com.univpm.fitquest.tracking.service.TrackingServiceController
import com.univpm.fitquest.tracking.service.TrackingServiceState
import com.univpm.fitquest.tracking.service.TrackingLifecycleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TrackMapUiState(
    val routePoints: List<InMemoryRoutePoint> = emptyList(),
    val previewLocation: PreviewLocation? = null,
    val lifecycleState: TrackingLifecycleState = TrackingLifecycleState.Idle,
)

data class TrackPanelUiState(
    val lifecycleState: TrackingLifecycleState = TrackingLifecycleState.Idle,
    val sport: Sport? = null,
    val distanceMeters: Double = 0.0,
    val currentSpeedMetersPerSecond: Float? = null,
    val currentAltitudeMeters: Double? = null,
    val estimatedCaloriesKcal: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val errorMessage: String? = null,
)

class TrackViewModel : ViewModel() {
    val trackingState: StateFlow<TrackingServiceState> = TrackingServiceController.state
    private val previewLocationFlow = MutableStateFlow<PreviewLocation?>(null)

    val mapState: StateFlow<TrackMapUiState> = trackingState
        .combine(previewLocationFlow) { state, previewLocation ->
            state.toMapUiState(previewLocation)
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = trackingState.value.toMapUiState(previewLocationFlow.value),
        )

    val panelState: StateFlow<TrackPanelUiState> = trackingState
        .map { it.toPanelUiState() }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = trackingState.value.toPanelUiState(),
        )

    val elapsedMillis: StateFlow<Long> = trackingState
        .map { it.elapsedMillis }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = trackingState.value.elapsedMillis,
        )

    fun setInitialPreviewLocation(location: PreviewLocation?) {
        if (location == null) return
        val state = trackingState.value
        if (state.lifecycleState == TrackingLifecycleState.Idle && state.routePoints.isEmpty()) {
            previewLocationFlow.value = location
        }
    }

    fun startTracking(context: Context, sport: Sport) {
        TrackingServiceController.startTracking(context, sport)
    }

    fun pauseTracking(context: Context) {
        TrackingServiceController.pauseTracking(context)
    }

    fun resumeTracking(context: Context) {
        TrackingServiceController.resumeTracking(context)
    }

    fun stopTracking(context: Context) {
        TrackingServiceController.stopTracking(context)
    }

    fun retrySave(context: Context) {
        TrackingServiceController.retrySave(context)
    }

    fun discardFailedSave(context: Context) {
        TrackingServiceController.discardFailedSave(context)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackViewModel() as T
        }
    }
}

internal fun TrackingServiceState.toMapUiState(previewLocation: PreviewLocation? = null): TrackMapUiState {
    return TrackMapUiState(
        routePoints = routePoints,
        previewLocation = previewLocation,
        lifecycleState = lifecycleState,
    )
}

internal fun TrackingServiceState.toPanelUiState(): TrackPanelUiState {
    return TrackPanelUiState(
        lifecycleState = lifecycleState,
        sport = sport,
        distanceMeters = distanceMeters,
        currentSpeedMetersPerSecond = currentSpeedMetersPerSecond,
        currentAltitudeMeters = currentAltitudeMeters,
        estimatedCaloriesKcal = estimatedCaloriesKcal,
        elevationGainMeters = elevationGainMeters,
        elevationLossMeters = elevationLossMeters,
        errorMessage = errorMessage,
    )
}
