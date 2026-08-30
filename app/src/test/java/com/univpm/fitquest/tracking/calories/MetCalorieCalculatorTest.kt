package com.univpm.fitquest.tracking.calories

import com.univpm.fitquest.domain.model.Sport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MetCalorieCalculatorTest {
    @Test
    fun zeroOrNegativeDurationReturnsZero() {
        assertEquals(0.0, estimate(Sport.Walking, 0L, 0.0, 80.0), 0.0)
        assertEquals(0.0, estimate(Sport.Walking, -1L, 1_000.0, 80.0), 0.0)
    }

    @Test
    fun invalidWeightFallsBackToSeventyKilograms() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, 19.9, 300.1).forEach { weight ->
            assertEquals(343.0, estimate(Sport.Running, 30 * 60_000L, 5_000.0, weight), 0.0)
        }
    }

    @Test
    fun validWeightUsesMetTimesKilogramsTimesHours() {
        assertEquals(392.0, estimate(Sport.Running, 30 * 60_000L, 5_000.0, 80.0), 0.0)
    }

    @Test
    fun distanceChangesAverageSpeedAndMetBand() {
        assertEquals(70.0, estimate(Sport.Walking, 60 * 60_000L, 1_000.0, 70.0), 0.0)
        assertEquals(210.0, estimate(Sport.Walking, 60 * 60_000L, 5_000.0, 70.0), 0.0)
    }

    @Test
    fun updatedDurationAndDistanceProduceDifferentCalorieEstimate() {
        val previousEstimate = estimate(Sport.Running, 59_000L, 150.0, 70.0)
        val updatedEstimate = estimate(Sport.Running, 60_000L, 210.0, 70.0)

        assertNotEquals(previousEstimate, updatedEstimate, 0.0)
        assertEquals(13.4166667, updatedEstimate, 0.0000001)
    }

    @Test
    fun elevationIsNotACalorieInput() {
        val method = MetCalorieCalculator::class.java.declaredMethods.single { it.name == "estimateKcal" }
        assertEquals(4, method.parameterCount)
    }

    @Test
    fun walkingMetBandsKeepBoundaries() {
        assertMet(Sport.Walking, 0.299, 1.0)
        assertMet(Sport.Walking, 0.3, 3.0)
        assertMet(Sport.Walking, 1.399, 3.0)
        assertMet(Sport.Walking, 1.4, 3.8)
        assertMet(Sport.Walking, 1.799, 3.8)
        assertMet(Sport.Walking, 1.8, 5.0)
    }

    @Test
    fun runningMetBandsKeepBoundaries() {
        assertMet(Sport.Running, 0.3, 7.0)
        assertMet(Sport.Running, 2.499, 7.0)
        assertMet(Sport.Running, 2.5, 9.8)
        assertMet(Sport.Running, 3.499, 9.8)
        assertMet(Sport.Running, 3.5, 11.5)
    }

    @Test
    fun cyclingMetBandsKeepBoundaries() {
        assertMet(Sport.Cycling, 0.3, 4.0)
        assertMet(Sport.Cycling, 4.499, 4.0)
        assertMet(Sport.Cycling, 4.5, 6.8)
        assertMet(Sport.Cycling, 6.699, 6.8)
        assertMet(Sport.Cycling, 6.7, 8.0)
    }

    private fun estimate(sport: Sport, duration: Long, distance: Double, weight: Double) =
        MetCalorieCalculator.estimateKcal(sport, duration, distance, weight)

    private fun assertMet(sport: Sport, speed: Double, expected: Double) {
        assertEquals(expected, MetCalorieCalculator.metFor(sport, speed), 0.0)
    }
}
