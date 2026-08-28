package com.univpm.fitquest.util

import android.text.format.DateFormat
import java.time.chrono.IsoChronology
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.util.Locale

object FormatUtils {
    private val locale = Locale.ITALIAN

    fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis.coerceAtLeast(0L) / 1_000
        val hours = totalSeconds / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(locale, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(locale, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatDurationMinutes(durationMillis: Long): Long = durationMillis.coerceAtLeast(0L) / 60_000L

    fun formatDistance(distanceMeters: Double): FormattedDistance {
        val safeDistance = distanceMeters.coerceAtLeast(0.0)
        return if (safeDistance < 1_000.0) {
            FormattedDistance(String.format(locale, "%.0f", safeDistance), DistanceUnit.Meters)
        } else {
            FormattedDistance(
                String.format(locale, "%.2f", safeDistance / 1_000.0),
                DistanceUnit.Kilometers,
            )
        }
    }

    fun formatOneDecimal(value: Double): String = String.format(locale, "%.1f", value)

    fun formatPace(speedMetersPerSecond: Float?): String {
        if (speedMetersPerSecond == null || speedMetersPerSecond <= 0.1f) return "--"
        val totalSecondsPerKm = (1_000 / speedMetersPerSecond).toInt()
        return String.format(locale, "%d:%02d", totalSecondsPerKm / 60, totalSecondsPerKm % 60)
    }

    fun formatSpeed(speedMetersPerSecond: Double): String =
        String.format(locale, "%.1f", speedMetersPerSecond.coerceAtLeast(0.0) * 3.6)

    fun formatWholeNumber(value: Double): String = String.format(locale, "%.0f", value.coerceAtLeast(0.0))
    fun formatTemperatureCelsius(value: Double): String = String.format(locale, "%.1f", value)

    fun formatCurrentDate(): String = runCatching {
        val pattern = runCatching {
            DateFormat.getBestDateTimePattern(locale, "EEEEMMMMd")
        }.getOrNull()?.takeIf(String::isNotBlank) ?: localizedDatePatternWithoutYear()
        ZonedDateTime.now().format(DateTimeFormatter.ofPattern(pattern, locale))
    }.getOrDefault("")

    private fun localizedDatePatternWithoutYear(): String =
        DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            FormatStyle.FULL,
            null,
            IsoChronology.INSTANCE,
            locale,
        ).replace(Regex("""[,\s]*y+[,\s]*"""), " ").trim()
}

data class FormattedDistance(
    val value: String,
    val unit: DistanceUnit,
)

enum class DistanceUnit {
    Meters,
    Kilometers,
}
