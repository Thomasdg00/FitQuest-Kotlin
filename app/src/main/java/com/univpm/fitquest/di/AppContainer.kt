package com.univpm.fitquest.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.univpm.fitquest.data.local.database.FitQuestDatabase
import com.univpm.fitquest.data.remote.OpenMeteoApi
import com.univpm.fitquest.data.repository.GoalRepository
import com.univpm.fitquest.data.repository.UserSettingsRepository
import com.univpm.fitquest.data.repository.WeatherRepository
import com.univpm.fitquest.data.repository.WorkoutRepository
import com.univpm.fitquest.tracking.location.FusedPreviewLocationProvider
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer internal constructor(
    val context: Context,
    databaseOverride: FitQuestDatabase? = null,
) {
    private val database: FitQuestDatabase by lazy {
        databaseOverride ?: FitQuestDatabase.getInstance(context)
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            workoutDao = database.workoutDao(),
            routePointDao = database.routePointDao(),
        )
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepository(database.goalDao())
    }

    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(database.userSettingsDao())
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val openMeteoApi: OpenMeteoApi by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_METEO_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoApi::class.java)
    }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(openMeteoApi)
    }

    val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val previewLocationProvider: FusedPreviewLocationProvider by lazy {
        FusedPreviewLocationProvider(fusedLocationClient)
    }

    companion object {
        private const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/"
    }
}
