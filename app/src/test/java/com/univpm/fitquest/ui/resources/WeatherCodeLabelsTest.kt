package com.univpm.fitquest.ui.resources

import com.univpm.fitquest.R
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeLabelsTest {
    @Test
    fun clearSkyUsesDedicatedWeatherLabel() {
        assertEquals(R.string.weather_condition_clear_sky, weatherCodeToLabelRes(0))
    }

    @Test
    fun thunderstormWithHailUsesThunderstormLabel() {
        assertEquals(R.string.weather_condition_thunderstorm, weatherCodeToLabelRes(96))
        assertEquals(R.string.weather_condition_thunderstorm, weatherCodeToLabelRes(99))
    }

    @Test
    fun freezingAndSnowCodesUseDedicatedLabels() {
        assertEquals(R.string.weather_condition_freezing_drizzle, weatherCodeToLabelRes(56))
        assertEquals(R.string.weather_condition_freezing_drizzle, weatherCodeToLabelRes(57))
        assertEquals(R.string.weather_condition_freezing_rain, weatherCodeToLabelRes(66))
        assertEquals(R.string.weather_condition_freezing_rain, weatherCodeToLabelRes(67))
        assertEquals(R.string.weather_condition_snow_grains, weatherCodeToLabelRes(77))
        assertEquals(R.string.weather_condition_snow_showers, weatherCodeToLabelRes(85))
        assertEquals(R.string.weather_condition_snow_showers, weatherCodeToLabelRes(86))
    }

    @Test
    fun unknownWeatherCodeUsesFallbackLabel() {
        assertEquals(R.string.weather_condition_unknown, weatherCodeToLabelRes(999))
    }
}
