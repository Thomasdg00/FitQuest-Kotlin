package com.univpm.fitquest.tracking.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingLocationRulesTest {
    @Test
    fun acceptsOnlyLocationsAtOrBelowTrackingAccuracyLimit() {
        assertTrue(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = 30f))
        assertFalse(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = 30.1f))
    }

    @Test
    fun acceptsLocationsAtOrBelowInitialAccuracyLimitIfInitialFix() {
        assertTrue(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = 500f, isInitialFix = true))
        assertFalse(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = 500.1f, isInitialFix = true))
    }

    @Test
    fun missingAccuracyDoesNotRejectValidCoordinates() {
        assertTrue(isUsableLocationSample(latitude = 43.6, longitude = 13.5, accuracyMeters = null))
    }

}
