package com.univpm.fitquest.ui.components

import com.univpm.fitquest.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleChartsTest {
    @Test
    fun chartAxisTicksUseReadableRoundedStepsAcrossDataRange() {
        assertEquals(
            listOf(0.0, 5.0, 10.0),
            buildChartAxisTicks(minValue = 1.2, maxValue = 9.8, preferredTickCount = 4),
        )
    }

    @Test
    fun chartAxisTicksExpandFlatDataAroundSingleValue() {
        assertEquals(
            listOf(10.0, 11.0, 12.0),
            buildChartAxisTicks(minValue = 11.0, maxValue = 11.0, preferredTickCount = 3),
        )
    }

    @Test
    fun chartLabelsUseItalianFormatting() {
        assertEquals("4,5", FormatUtils.formatOneDecimal(4.5))
        assertEquals("5:30", formatChartPace(5.5))
        assertEquals("123", formatChartMeters(123.4))
        assertEquals("5,3", FormatUtils.formatOneDecimal(5.25))
        assertEquals("2,5", FormatUtils.formatOneDecimal(2.5))
    }
}
