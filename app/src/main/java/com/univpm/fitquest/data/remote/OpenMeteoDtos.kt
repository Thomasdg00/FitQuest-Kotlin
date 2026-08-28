package com.univpm.fitquest.data.remote

import com.squareup.moshi.Json


data class OpenMeteoDailyResponseDto(
    @param:Json(name = "daily") val daily: OpenMeteoDailyDto?,
)

data class OpenMeteoDailyDto(
    @param:Json(name = "time") val dates: List<String>?,
    @param:Json(name = "weather_code") val weatherCodes: List<Int?>?,
    @param:Json(name = "temperature_2m_max") val maxTemperatures: List<Double?>?,
    @param:Json(name = "temperature_2m_min") val minTemperatures: List<Double?>?,
)
