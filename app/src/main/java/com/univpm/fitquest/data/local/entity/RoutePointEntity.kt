package com.univpm.fitquest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ordered point in a workout route. Accuracy is meters when Android provides it.
 */
@Entity(
    tableName = "route_points",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutId", "recordedAtMillis"]),
        Index(value = ["workoutId", "sequenceIndex"])
    ]
)
data class RoutePointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val sequenceIndex: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val recordedAtMillis: Long,
    val altitudeMeters: Double? = null,
    val accuracyMeters: Float? = null,
    val speedMetersPerSecond: Float? = null
)
