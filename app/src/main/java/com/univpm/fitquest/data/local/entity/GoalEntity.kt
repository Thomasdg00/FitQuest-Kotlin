package com.univpm.fitquest.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Weekly distance goal for one sport. Distance is stored in meters.
 */
@Entity(
    tableName = "goals",
    indices = [
        Index(value = ["sport"], unique = true)
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sport: String,
    val weeklyDistanceGoalMeters: Double,
    val updatedAtMillis: Long
)
