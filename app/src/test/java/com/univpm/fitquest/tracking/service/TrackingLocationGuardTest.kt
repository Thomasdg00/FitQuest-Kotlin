package com.univpm.fitquest.tracking.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingLocationGuardTest {
    @Test
    fun pauseInvalidatesOutstandingGenerationAndResumeCreatesAnother() {
        val guard = TrackingLocationGuard()
        val beforePause = guard.onStart()

        assertTrue(guard.allowsCallback(beforePause, TrackingLifecycleState.Running))
        guard.invalidateSession()
        assertFalse(guard.allowsCallback(beforePause, TrackingLifecycleState.Running))

        val afterResume = guard.invalidateSession()
        assertTrue(afterResume > beforePause)
        assertTrue(guard.allowsCallback(afterResume, TrackingLifecycleState.Running))
    }

    @Test
    fun stopDiscardAndNewSessionRejectPreviousCallbacks() {
        val guard = TrackingLocationGuard()
        val stoppedGeneration = guard.onStart()
        guard.invalidateSession()
        assertFalse(guard.allowsCallback(stoppedGeneration, TrackingLifecycleState.Running))

        val discardedGeneration = guard.onStart()
        guard.invalidateSession()
        assertFalse(guard.allowsCallback(discardedGeneration, TrackingLifecycleState.Running))

        val previousSession = guard.onStart()
        val currentSession = guard.onStart()
        assertFalse(guard.allowsCallback(previousSession, TrackingLifecycleState.Running))
        assertTrue(guard.allowsCallback(currentSession, TrackingLifecycleState.Running))
    }

    @Test
    fun cachedLocationRequiresReliableAgeInsideLimit() {
        val guard = TrackingLocationGuard()
        val now = 100_000_000_000L

        assertTrue(
            guard.isCachedLocationFresh(
                elapsedRealtimeNanos = now - MAX_CACHED_LOCATION_AGE_MS * 1_000_000L,
                nowElapsedRealtimeNanos = now,
            ),
        )
        assertFalse(
            guard.isCachedLocationFresh(
                elapsedRealtimeNanos = now - (MAX_CACHED_LOCATION_AGE_MS + 1L) * 1_000_000L,
                nowElapsedRealtimeNanos = now,
            ),
        )
        assertFalse(guard.isCachedLocationFresh(0L, now))
        assertFalse(guard.isCachedLocationFresh(now + 1L, now))
    }

    @Test
    fun equalOlderAndDuplicateSamplesAreRejected() {
        val guard = TrackingLocationGuard()
        val generation = guard.onStart()

        assertEquals(
            AcceptedLocationSample.Baseline,
            guard.acceptSample(generation, TrackingLifecycleState.Running, 1_000L),
        )
        assertNull(guard.acceptSample(generation, TrackingLifecycleState.Running, 1_000L))
        assertNull(guard.acceptSample(generation, TrackingLifecycleState.Running, 999L))
        assertEquals(
            AcceptedLocationSample.Movement,
            guard.acceptSample(generation, TrackingLifecycleState.Running, 2_000L),
        )
    }

    @Test
    fun prePauseCallbackCannotBecomePostResumeBaseline() {
        val guard = TrackingLocationGuard()
        val beforePause = guard.onStart()
        assertEquals(
            AcceptedLocationSample.Baseline,
            guard.acceptSample(beforePause, TrackingLifecycleState.Running, 1_000L),
        )

        guard.invalidateSession()
        val afterResume = guard.invalidateSession()
        assertNull(
            guard.acceptSample(beforePause, TrackingLifecycleState.Running, 2_000L),
        )
        assertEquals(
            AcceptedLocationSample.Baseline,
            guard.acceptSample(afterResume, TrackingLifecycleState.Running, 3_000L),
        )
        assertEquals(
            AcceptedLocationSample.Movement,
            guard.acceptSample(afterResume, TrackingLifecycleState.Running, 4_000L),
        )
        assertNull(
            guard.acceptSample(afterResume, TrackingLifecycleState.Running, 4_000L),
        )
    }
}
