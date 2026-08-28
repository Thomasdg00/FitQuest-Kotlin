package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRoutePoints(points: List<RoutePointEntity>)

    @Transaction
    open suspend fun insertWithRoutePoints(
        workout: WorkoutEntity,
        routePoints: List<RoutePointEntity>,
    ): Long {
        val workoutId = insertWorkout(workout)
        if (routePoints.isNotEmpty()) {
            insertRoutePoints(routePoints.map { it.copy(workoutId = workoutId) })
        }
        return workoutId
    }

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    abstract suspend fun deleteById(workoutId: Long): Int

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    abstract fun observeById(workoutId: Long): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts ORDER BY startedAtMillis DESC")
    abstract fun observeAll(): Flow<List<WorkoutEntity>>
}
