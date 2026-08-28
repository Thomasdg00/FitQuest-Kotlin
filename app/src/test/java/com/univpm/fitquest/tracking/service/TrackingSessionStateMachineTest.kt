package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingSessionStateMachineTest {
    @Test
    fun duplicateStartDoesNotResetTheAcceptedSession() {
        val stateMachine = TrackingSessionStateMachine()
        val route = mutableListOf("existing-point")
        var sport: Sport? = null
        var startedAtMillis: Long? = null
        var initializations = 0
        var tickerStarts = 0
        var locationSubscriptions = 0
        var foregroundStarts = 0

        fun sendStart(requestedSport: Sport, requestedStartMillis: Long) {
            if (!stateMachine.beginStart()) return

            initializations++
            tickerStarts++
            locationSubscriptions++
            foregroundStarts++
            route.clear()
            sport = requestedSport
            startedAtMillis = requestedStartMillis
        }

        sendStart(Sport.Running, requestedStartMillis = 1_000L)
        route += "accepted-after-start"
        sendStart(Sport.Cycling, requestedStartMillis = 2_000L)

        assertEquals(1, initializations)
        assertEquals(1, tickerStarts)
        assertEquals(1, locationSubscriptions)
        assertEquals(1, foregroundStarts)
        assertEquals(listOf("accepted-after-start"), route)
        assertEquals(Sport.Running, sport)
        assertEquals(1_000L, startedAtMillis)

        assertTrue(stateMachine.pause())
        assertFalse(stateMachine.beginStart())
    }

    @Test
    fun duplicateStopKeepsOneInFlightSaveAndOneFinalResult() {
        val stateMachine = TrackingSessionStateMachine()
        val firstRequest = saveRequest(endedAtMillis = 2_000L)
        val duplicateRequest = saveRequest(endedAtMillis = 3_000L)
        val save = ControllableSave()
        var finalResults = 0
        var stopSelfCalls = 0

        fun sendStop(request: CompletedWorkoutSaveRequest) {
            if (!stateMachine.beginStop(request)) return
            save.start(request)
        }

        assertTrue(stateMachine.beginStart())
        sendStop(firstRequest)
        sendStop(duplicateRequest)

        assertEquals(1, save.startCount)
        assertTrue(save.isActive)
        assertFalse(stateMachine.beginStart())
        assertSame(firstRequest, save.request)
        assertSame(firstRequest, stateMachine.pendingSave)
        assertEquals(TrackingLifecycleState.Stopping, stateMachine.lifecycleState)
        assertEquals(0, stopSelfCalls)

        if (stateMachine.onSaveSucceeded()) {
            save.complete()
            finalResults++
            stopSelfCalls++
        }

        assertFalse(save.isActive)
        assertEquals(1, finalResults)
        assertEquals(1, stopSelfCalls)
        assertEquals(TrackingLifecycleState.Idle, stateMachine.lifecycleState)
    }

    @Test
    fun failedSaveRetainsSnapshotAndRetryClearsItOnlyAfterSuccess() {
        val stateMachine = TrackingSessionStateMachine()
        val request = saveRequest(endedAtMillis = 2_000L)
        val launchedRequests = mutableListOf<CompletedWorkoutSaveRequest>()

        assertTrue(stateMachine.beginStart())
        assertTrue(stateMachine.beginStop(request))
        launchedRequests += request

        assertTrue(stateMachine.onSaveFailed())
        assertEquals(TrackingLifecycleState.SaveFailed, stateMachine.lifecycleState)
        assertSame(request, stateMachine.pendingSave)
        assertFalse(stateMachine.beginStart())
        assertFalse(stateMachine.beginStop(saveRequest(endedAtMillis = 3_000L)))

        val retryRequest = stateMachine.beginRetry()
        assertSame(request, retryRequest)
        retryRequest?.let(launchedRequests::add)
        assertNull(stateMachine.beginRetry())
        assertEquals(listOf(request, request), launchedRequests)
        assertSame(request, stateMachine.pendingSave)

        assertTrue(stateMachine.onSaveSucceeded())
        assertNull(stateMachine.pendingSave)
        assertEquals(TrackingLifecycleState.Idle, stateMachine.lifecycleState)
        assertFalse(stateMachine.onSaveSucceeded())
    }

    @Test
    fun failedSaveCanBeDiscardedExactlyOnce() {
        val stateMachine = TrackingSessionStateMachine()
        val request = saveRequest(endedAtMillis = 2_000L)

        assertTrue(stateMachine.beginStart())
        assertTrue(stateMachine.beginStop(request))
        assertTrue(stateMachine.onSaveFailed())
        assertSame(request, stateMachine.pendingSave)

        assertTrue(stateMachine.beginDiscardFailedSave())
        assertEquals(TrackingLifecycleState.Idle, stateMachine.lifecycleState)
        assertNull(stateMachine.pendingSave)
        assertFalse(stateMachine.beginDiscardFailedSave())
    }

    @Test
    fun discardIsRejectedWhileRunningOrStopping() {
        val stateMachine = TrackingSessionStateMachine()
        val request = saveRequest(endedAtMillis = 2_000L)

        assertTrue(stateMachine.beginStart())
        assertFalse(stateMachine.beginDiscardFailedSave())
        assertTrue(stateMachine.beginStop(request))
        assertFalse(stateMachine.beginDiscardFailedSave())
        assertSame(request, stateMachine.pendingSave)
    }

    private fun saveRequest(endedAtMillis: Long): CompletedWorkoutSaveRequest {
        return CompletedWorkoutSaveRequest(
            sport = Sport.Running,
            startedAtMillis = 1_000L,
            endedAtMillis = endedAtMillis,
            durationMillis = 1_000L,
            distanceMeters = 25.0,
            routeSnapshot = listOf(
                InMemoryRoutePoint(
                    latitude = 43.6,
                    longitude = 13.5,
                    recordedAtMillis = 1_500L,
                    altitudeMeters = 10.0,
                    accuracyMeters = 5f,
                    speedMetersPerSecond = 2f,
                ),
            ),
            caloriesKcal = 5.0,
            elevationGainMeters = 2.0,
            elevationLossMeters = 1.0,
        )
    }

    private class ControllableSave {
        var startCount: Int = 0
            private set
        var isActive: Boolean = false
            private set
        var request: CompletedWorkoutSaveRequest? = null
            private set

        fun start(request: CompletedWorkoutSaveRequest) {
            startCount++
            isActive = true
            this.request = request
        }

        fun complete() {
            isActive = false
        }
    }
}
