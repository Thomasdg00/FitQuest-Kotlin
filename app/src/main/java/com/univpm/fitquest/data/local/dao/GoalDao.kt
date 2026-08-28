package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.univpm.fitquest.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE sport = :sport")
    suspend fun getForSport(sport: String): GoalEntity?

    @Query("SELECT * FROM goals ORDER BY sport ASC")
    fun observeAll(): Flow<List<GoalEntity>>
}
