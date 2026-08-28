package com.univpm.fitquest.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun dailyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String,
        @Query("forecast_days") forecastDays: Int,
        @Query("timezone") timezone: String,
    ): OpenMeteoDailyResponseDto
}

internal const val OPEN_METEO_DAILY_FIELDS =
    "weather_code,temperature_2m_max,temperature_2m_min"
internal const val OPEN_METEO_FORECAST_DAYS = 3
internal const val OPEN_METEO_TIMEZONE = "auto"
