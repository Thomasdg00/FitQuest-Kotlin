package com.univpm.fitquest.util

import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatUtilsTest {
    @Test
    fun formatsDurationAsTimerText() {
        assertEquals("00:05", FormatUtils.formatDuration(5_000L))
        assertEquals("01:02", FormatUtils.formatDuration(62_000L))
        assertEquals("1:01:01", FormatUtils.formatDuration(3_661_000L))
        assertEquals(61L, FormatUtils.formatDurationMinutes(3_661_000L))
    }

    @Test
    fun formatsDistanceWithItalianNumbers() {
        assertEquals(FormattedDistance("850", DistanceUnit.Meters), FormatUtils.formatDistance(850.0))
        assertEquals(FormattedDistance("1,25", DistanceUnit.Kilometers), FormatUtils.formatDistance(1_250.0))
    }

    @Test
    fun formatsMetricsWithItalianNumbers() {
        assertEquals("5:00", FormatUtils.formatPace(1_000f / 300f))
        assertEquals("--", FormatUtils.formatPace(0.0f))
        assertEquals("12,0", FormatUtils.formatSpeed(1_000.0 / 300.0))
        assertEquals("343", FormatUtils.formatWholeNumber(342.6))
        assertEquals("12", FormatUtils.formatWholeNumber(12.4))
        assertEquals("4,5", FormatUtils.formatOneDecimal(4.45))
        assertEquals("70,0", FormatUtils.formatOneDecimal(70.0))
        assertEquals("13", FormatUtils.formatWholeNumber(12.6))
        assertEquals("18,4", FormatUtils.formatTemperatureCelsius(18.4))
    }

    @Test
    fun currentDateUsesItalianNames() {
        val now = ZonedDateTime.now()
        val formatted = FormatUtils.formatCurrentDate()
        assertTrue(formatted.contains(now.dayOfMonth.toString()))
        assertTrue(formatted.contains(now.month.getDisplayName(TextStyle.FULL, Locale.ITALIAN)))
        assertTrue(formatted.contains(now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ITALIAN)))
    }
}
