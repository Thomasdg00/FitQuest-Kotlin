package com.univpm.fitquest.ui.resources

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.univpm.fitquest.R
import com.univpm.fitquest.ui.components.formatChartPace
import com.univpm.fitquest.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetricFormattingTest {
    @Test
    fun metricPatternsUseItalianNumbersAndResourceBackedUnits() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("5,25 km", context.formatDistanceMetric(5_250.0))
        assertEquals("850 m", context.formatDistanceMetric(850.0))
        assertEquals("12,4 km/h", context.formatSpeedMetric(12.4 / 3.6))
        assertEquals("5:00 min/km", context.formatPaceMetric(1_000f / 300f))
        assertEquals("343 kcal", context.formatCaloriesMetric(342.6))
        assertEquals("12 m", context.formatElevationMetric(12.4))
        assertEquals("5 min", context.formatDurationMinutesMetric(5))
        assertEquals("18,4 °C", context.formatTemperatureMetric(18.4))
        assertEquals("4,5 min", context.getString(R.string.metric_duration_minutes, FormatUtils.formatOneDecimal(4.5)))
        assertEquals("5:30 min/km", context.getString(R.string.metric_pace_min_km, formatChartPace(5.5)))
        assertEquals("5,3 km", context.getString(R.string.metric_distance_km, FormatUtils.formatOneDecimal(5.25)))
    }
}
