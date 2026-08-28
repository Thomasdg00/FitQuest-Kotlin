package com.univpm.fitquest.data.remote

import com.univpm.fitquest.domain.model.DailyForecast


internal fun OpenMeteoDailyResponseDto.toDailyForecasts(): List<DailyForecast> {
    val dailyForecast = daily ?: return emptyList()
    val dates = dailyForecast.dates.orEmpty()
    return dates.take(OPEN_METEO_FORECAST_DAYS).mapIndexed { index, date ->
        DailyForecast(
            date = date,
            weatherCode = dailyForecast.weatherCodes?.getOrNull(index),
            maxTemperatureCelsius = dailyForecast.maxTemperatures?.getOrNull(index),
            minTemperatureCelsius = dailyForecast.minTemperatures?.getOrNull(index),
        )
    }.filter { it.date.isNotBlank() }
}
