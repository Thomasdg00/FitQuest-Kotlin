package com.univpm.fitquest.ui.resources

import android.content.Context
import com.univpm.fitquest.R
import com.univpm.fitquest.util.DistanceUnit
import com.univpm.fitquest.util.FormatUtils

fun Context.formatDistanceMetric(distanceMeters: Double): String {
    val distance = FormatUtils.formatDistance(distanceMeters)
    val resource = when (distance.unit) {
        DistanceUnit.Meters -> R.string.metric_distance_m
        DistanceUnit.Kilometers -> R.string.metric_distance_km
    }
    return getString(resource, distance.value)
}

fun Context.formatKilometersMetric(value: Double): String =
    getString(R.string.metric_distance_km, FormatUtils.formatOneDecimal(value))

fun Context.formatSpeedMetric(speedMetersPerSecond: Double): String =
    getString(R.string.metric_speed_kmh, FormatUtils.formatSpeed(speedMetersPerSecond))

fun Context.formatSpeedMetric(speedMetersPerSecond: Float): String =
    formatSpeedMetric(speedMetersPerSecond.toDouble())

fun Context.formatPaceMetric(speedMetersPerSecond: Float?): String {
    val value = FormatUtils.formatPace(speedMetersPerSecond)
    return if (value == "--") value else getString(R.string.metric_pace_min_km, value)
}

fun Context.formatCaloriesMetric(value: Double): String =
    getString(R.string.metric_calories_kcal, FormatUtils.formatWholeNumber(value))

fun Context.formatElevationMetric(value: Double): String =
    getString(R.string.metric_elevation_m, FormatUtils.formatWholeNumber(value))

fun Context.formatDurationMinutesMetric(value: Long): String =
    getString(R.string.metric_duration_minutes, value.toString())

fun Context.formatTemperatureMetric(value: Double): String =
    getString(
        R.string.metric_temperature_celsius,
        FormatUtils.formatTemperatureCelsius(value),
    )

fun Context.formatTemperatureRangeMetric(minimum: Double, maximum: Double): String =
    getString(
        R.string.metric_temperature_range_celsius,
        FormatUtils.formatTemperatureCelsius(minimum),
        FormatUtils.formatTemperatureCelsius(maximum),
    )

fun Context.formatKilometerProgressMetric(progress: Double, target: Double): String =
    getString(
        R.string.metric_progress_km,
        FormatUtils.formatOneDecimal(progress),
        FormatUtils.formatOneDecimal(target),
    )
