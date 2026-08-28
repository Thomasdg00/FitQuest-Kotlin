package com.univpm.fitquest.domain.model

data class DailyForecast(
    val date: String,
    val weatherCode: Int?,
    val maxTemperatureCelsius: Double?,
    val minTemperatureCelsius: Double?,
)
