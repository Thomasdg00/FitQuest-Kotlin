package com.univpm.fitquest.tracking.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.univpm.fitquest.domain.model.Sport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TrackingServiceController {
    private val mutableState = MutableStateFlow(TrackingServiceState())
    val state: StateFlow<TrackingServiceState> = mutableState.asStateFlow()

    fun startTracking(context: Context, sport: Sport) {
        val intent = serviceIntent(context, TrackingService.ACTION_START).apply {
            putExtra(TrackingService.EXTRA_SPORT, sport.routeValue)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseTracking(context: Context) {
        context.startService(serviceIntent(context, TrackingService.ACTION_PAUSE))
    }

    fun resumeTracking(context: Context) {
        context.startService(serviceIntent(context, TrackingService.ACTION_RESUME))
    }

    fun stopTracking(context: Context) {
        context.startService(serviceIntent(context, TrackingService.ACTION_STOP))
    }

    fun retrySave(context: Context) {
        context.startService(serviceIntent(context, TrackingService.ACTION_RETRY_SAVE))
    }

    fun discardFailedSave(context: Context) {
        context.startService(serviceIntent(context, TrackingService.ACTION_DISCARD_FAILED_SAVE))
    }

    internal fun updateState(state: TrackingServiceState) {
        mutableState.value = state
    }

    private fun serviceIntent(context: Context, action: String): Intent {
        return Intent(context, TrackingService::class.java)
            .setAction(action)
    }
}
