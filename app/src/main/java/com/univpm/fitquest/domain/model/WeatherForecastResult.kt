package com.univpm.fitquest.domain.model

sealed interface WeatherForecastResult {
    data object Loading : WeatherForecastResult
    data class Success(val forecasts: List<DailyForecast>) : WeatherForecastResult
    data object Empty : WeatherForecastResult
    data object Error : WeatherForecastResult
}
