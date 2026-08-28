package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.remote.OpenMeteoApi
import com.univpm.fitquest.data.remote.OpenMeteoDailyDto
import com.univpm.fitquest.data.remote.OpenMeteoDailyResponseDto
import com.univpm.fitquest.domain.model.WeatherForecastResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherRepositoryTest {

    @Test
    fun fetchDailyForecastReturnsErrorOnFailure() = runBlocking {
        val repository = WeatherRepository(api = FakeOpenMeteoApi(dailyFailure = true))

        val result = repository.fetchDailyForecast(latitude = 43.0, longitude = 13.0)

        assertEquals(WeatherForecastResult.Error, result)
    }

    @Test
    fun fetchDailyForecastReturnsEmptyWhenResponseHasNoForecasts() = runBlocking {
        val repository = WeatherRepository(api = FakeOpenMeteoApi())

        val result = repository.fetchDailyForecast(latitude = 43.0, longitude = 13.0)

        assertEquals(WeatherForecastResult.Empty, result)
    }

    @Test
    fun fetchDailyForecastMapsSuccessfulResponse() = runBlocking {
        val repository = WeatherRepository(
            api = FakeOpenMeteoApi(
                dailyResponse = OpenMeteoDailyResponseDto(
                    daily = OpenMeteoDailyDto(
                        dates = listOf("2026-06-10"),
                        weatherCodes = listOf(0),
                        maxTemperatures = listOf(24.0),
                        minTemperatures = listOf(14.0),
                    ),
                ),
            ),
        )

        val result = repository.fetchDailyForecast(latitude = 43.0, longitude = 13.0)
        val forecast = (result as WeatherForecastResult.Success).forecasts

        assertEquals(1, forecast.size)
        assertEquals("2026-06-10", forecast[0].date)
        assertEquals(0, forecast[0].weatherCode)
    }
}

private class FakeOpenMeteoApi(
    private val dailyResponse: OpenMeteoDailyResponseDto = OpenMeteoDailyResponseDto(daily = null),
    private val dailyFailure: Boolean = false,
) : OpenMeteoApi {

    override suspend fun dailyForecast(
        latitude: Double,
        longitude: Double,
        daily: String,
        forecastDays: Int,
        timezone: String,
    ): OpenMeteoDailyResponseDto {
        if (dailyFailure) error("daily failed")
        return dailyResponse
    }
}
