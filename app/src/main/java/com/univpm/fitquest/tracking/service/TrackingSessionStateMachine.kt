package com.univpm.fitquest.tracking.service

internal class TrackingSessionStateMachine {
    var lifecycleState: TrackingLifecycleState = TrackingLifecycleState.Idle
        private set

    var pendingSave: CompletedWorkoutSaveRequest? = null
        private set

    fun beginStart(): Boolean {
        if (lifecycleState != TrackingLifecycleState.Idle) return false

        lifecycleState = TrackingLifecycleState.Running
        return true
    }

    fun pause(): Boolean {
        if (lifecycleState != TrackingLifecycleState.Running) return false

        lifecycleState = TrackingLifecycleState.Paused
        return true
    }

    fun resume(): Boolean {
        if (lifecycleState != TrackingLifecycleState.Paused) return false

        lifecycleState = TrackingLifecycleState.Running
        return true
    }

    fun beginStop(request: CompletedWorkoutSaveRequest): Boolean {
        if (
            lifecycleState != TrackingLifecycleState.Running &&
            lifecycleState != TrackingLifecycleState.Paused
        ) {
            return false
        }

        lifecycleState = TrackingLifecycleState.Stopping
        pendingSave = request
        return true
    }

    fun onSaveFailed(): Boolean {
        if (
            lifecycleState != TrackingLifecycleState.Stopping ||
            pendingSave == null
        ) {
            return false
        }

        lifecycleState = TrackingLifecycleState.SaveFailed
        return true
    }

    fun beginRetry(): CompletedWorkoutSaveRequest? {
        if (lifecycleState != TrackingLifecycleState.SaveFailed) return null

        val request = pendingSave ?: return null
        lifecycleState = TrackingLifecycleState.Stopping
        return request
    }

    fun beginDiscardFailedSave(): Boolean {
        if (
            lifecycleState != TrackingLifecycleState.SaveFailed ||
            pendingSave == null
        ) {
            return false
        }

        lifecycleState = TrackingLifecycleState.Idle
        pendingSave = null
        return true
    }

    fun onSaveSucceeded(): Boolean {
        if (
            lifecycleState != TrackingLifecycleState.Stopping ||
            pendingSave == null
        ) {
            return false
        }

        lifecycleState = TrackingLifecycleState.Idle
        pendingSave = null
        return true
    }

    fun reset() {
        lifecycleState = TrackingLifecycleState.Idle
        pendingSave = null
    }
}
