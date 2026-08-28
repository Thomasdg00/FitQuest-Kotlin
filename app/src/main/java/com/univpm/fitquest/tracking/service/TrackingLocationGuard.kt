package com.univpm.fitquest.tracking.service

internal const val MAX_CACHED_LOCATION_AGE_MS = 30_000L

internal enum class AcceptedLocationSample {
    Baseline,
    Movement,
}

internal class TrackingLocationGuard(
    private val maxCachedLocationAgeMillis: Long = MAX_CACHED_LOCATION_AGE_MS,
) {
    var generation: Long = 0L
        private set

    var hasBaseline: Boolean = false
        private set

    var lastAcceptedElapsedRealtimeNanos: Long? = null
        private set

    fun onStart(): Long {
        lastAcceptedElapsedRealtimeNanos = null
        return advanceGeneration()
    }

    fun invalidateSession(): Long = advanceGeneration()

    fun allowsCallback(
        capturedGeneration: Long,
        lifecycleState: TrackingLifecycleState,
    ): Boolean {
        return capturedGeneration == generation &&
            lifecycleState == TrackingLifecycleState.Running
    }

    fun canAcceptSample(
        capturedGeneration: Long,
        lifecycleState: TrackingLifecycleState,
        elapsedRealtimeNanos: Long,
    ): Boolean {
        if (!allowsCallback(capturedGeneration, lifecycleState)) return false
        if (elapsedRealtimeNanos <= 0L) return false

        val previous = lastAcceptedElapsedRealtimeNanos
        return previous == null || elapsedRealtimeNanos > previous
    }

    fun acceptSample(
        capturedGeneration: Long,
        lifecycleState: TrackingLifecycleState,
        elapsedRealtimeNanos: Long,
    ): AcceptedLocationSample? {
        if (!canAcceptSample(capturedGeneration, lifecycleState, elapsedRealtimeNanos)) {
            return null
        }

        val result = if (hasBaseline) {
            AcceptedLocationSample.Movement
        } else {
            AcceptedLocationSample.Baseline
        }
        lastAcceptedElapsedRealtimeNanos = elapsedRealtimeNanos
        hasBaseline = true
        return result
    }

    fun isCachedLocationFresh(
        elapsedRealtimeNanos: Long,
        nowElapsedRealtimeNanos: Long,
    ): Boolean {
        if (
            elapsedRealtimeNanos <= 0L ||
            nowElapsedRealtimeNanos <= 0L ||
            elapsedRealtimeNanos > nowElapsedRealtimeNanos
        ) {
            return false
        }

        val ageNanos = nowElapsedRealtimeNanos - elapsedRealtimeNanos
        return ageNanos <= maxCachedLocationAgeMillis * NANOS_PER_MILLISECOND
    }

    private fun advanceGeneration(): Long {
        generation++
        hasBaseline = false
        return generation
    }

    private companion object {
        const val NANOS_PER_MILLISECOND = 1_000_000L
    }
}
