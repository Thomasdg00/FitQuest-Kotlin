package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Query("SELECT * FROM route_points WHERE workoutId = :workoutId ORDER BY sequenceIndex ASC, recordedAtMillis ASC")
    fun observeForWorkout(workoutId: Long): Flow<List<RoutePointEntity>>
}
