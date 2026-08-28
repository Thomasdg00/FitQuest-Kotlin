package com.univpm.fitquest.tracking.service

import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.tracking.calories.MetCalorieCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingServiceTest {

    @Test
    fun locationSamplesRejectInvalidCoordinates() {
        assertFalse(isUsableLocationSample(latitude = 91.0, longitude = 13.5, accuracyMeters = 5f))
        assertFalse(isUsableLocationSample(latitude = 43.6, longitude = -181.0, accuracyMeters = 5f))
        assertTrue(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = 5f))
    }

    @Test
    fun finalSaveCaloriesUseFinalDurationAndDistanceInsteadOfCachedEstimate() {
        val cachedEstimate = MetCalorieCalculator.estimateKcal(Sport.Running, 59_000L, 150.0, 70.0)
        val finalCalories = MetCalorieCalculator.estimateKcal(Sport.Running, 60_000L, 210.0, 70.0)

        assertNotEquals(cachedEstimate, finalCalories, 0.0)
        assertEquals(13.4166667, finalCalories, 0.0000001)
    }
}
