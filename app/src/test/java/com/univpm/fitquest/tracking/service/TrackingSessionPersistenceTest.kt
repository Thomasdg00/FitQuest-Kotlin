package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingSessionPersistenceTest {
    @Test
    fun buildTrackedWorkoutEntityCreatesCompletedWorkoutWithAverageSpeed() {
        val workout = buildTrackedWorkoutEntity(
            sport = Sport.Running,
            startedAtMillis = 1_000L,
            endedAtMillis = 61_000L,
            durationMillis = 60_000L,
            distanceMeters = 300.0,
        )

        assertEquals(Sport.Running.routeValue, workout.sport)
        assertEquals(1_000L, workout.startedAtMillis)
        assertEquals(61_000L, workout.endedAtMillis)
        assertEquals(60_000L, workout.durationMillis)
        assertEquals(300.0, workout.distanceMeters, 0.0)
        assertEquals(5.0, workout.averageSpeedMetersPerSecond, 0.0)
        assertEquals(true, workout.isCompleted)
    }

    @Test
    fun routePointEntitiesKeepWorkoutLinkAndSequenceOrder() {
        val points = listOf(
            InMemoryRoutePoint(
                latitude = 43.6167,
                longitude = 13.5167,
                recordedAtMillis = 2_000L,
                altitudeMeters = 35.0,
                accuracyMeters = 4.5f,
                speedMetersPerSecond = 2.0f,
            ),
            InMemoryRoutePoint(
                latitude = 43.6170,
                longitude = 13.5170,
                recordedAtMillis = 2_000L,
                altitudeMeters = null,
                accuracyMeters = null,
                speedMetersPerSecond = null,
            ),
        )

        val entities = points.toRoutePointEntities(workoutId = 42L)

        assertEquals(2, entities.size)
        assertEquals(42L, entities[0].workoutId)
        assertEquals(0, entities[0].sequenceIndex)
        assertEquals(1, entities[1].sequenceIndex)
        assertEquals(43.6167, entities[0].latitude, 0.0)
        assertEquals(13.5170, entities[1].longitude, 0.0)
        assertEquals(35.0, entities[0].altitudeMeters ?: 0.0, 0.0)
    }
}
