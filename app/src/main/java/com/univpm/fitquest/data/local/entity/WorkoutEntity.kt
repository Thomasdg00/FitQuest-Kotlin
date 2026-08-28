package com.univpm.fitquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One workout session. Distances are meters, times are Unix epoch milliseconds.
 */
@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sport: String,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val durationMillis: Long = 0,
    val distanceMeters: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val averageSpeedMetersPerSecond: Double = 0.0,
    val caloriesKcal: Double = 0.0,
    val isCompleted: Boolean = false
)
