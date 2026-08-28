package com.univpm.fitquest.tracking.calories

import com.univpm.fitquest.domain.model.Sport

object MetCalorieCalculator {
    fun estimateKcal(
        sport: Sport,
        activeDurationMillis: Long,
        distanceMeters: Double,
        bodyWeightKg: Double,
    ): Double {
        if (activeDurationMillis <= 0L) return 0.0

        val activeSeconds = activeDurationMillis.coerceAtLeast(0L) / 1_000.0
        val averageSpeedMetersPerSecond = distanceMeters.coerceAtLeast(0.0) / activeSeconds
        val activeDurationHours = activeDurationMillis / 3_600_000.0
        val resolvedWeightKg = bodyWeightKg.takeIf { it.isFinite() && it in 20.0..300.0 }
            ?: DEFAULT_WEIGHT_KG
        return (metFor(sport, averageSpeedMetersPerSecond) * resolvedWeightKg * activeDurationHours)
            .coerceAtLeast(0.0)
    }

    /**
     * Simplified project-specific speed bands informed by MET concepts and intensity
     * orders of magnitude in the 2024 Adult Compendium; not a literal table reproduction.
     * Source: Herrmann SD, Willis EA, Ainsworth BE, et al., 2024 Adult Compendium of
     * Physical Activities: A third update of the energy costs of human activities,
     * Journal of Sport and Health Science 13(1), 2024, 6-12,
     * doi:10.1016/j.jshs.2023.10.010; https://pacompendium.com/
     */
    fun metFor(sport: Sport, averageSpeedMetersPerSecond: Double): Double {
        if (averageSpeedMetersPerSecond < 0.3) return 1.0

        return when (sport) {
            Sport.Walking -> when {
                averageSpeedMetersPerSecond < 1.4 -> 3.0
                averageSpeedMetersPerSecond < 1.8 -> 3.8
                else -> 5.0
            }
            Sport.Running -> when {
                averageSpeedMetersPerSecond < 2.5 -> 7.0
                averageSpeedMetersPerSecond < 3.5 -> 9.8
                else -> 11.5
            }
            Sport.Cycling -> when {
                averageSpeedMetersPerSecond < 4.5 -> 4.0
                averageSpeedMetersPerSecond < 6.7 -> 6.8
                else -> 8.0
            }
        }
    }

    private const val DEFAULT_WEIGHT_KG = 70.0
}
