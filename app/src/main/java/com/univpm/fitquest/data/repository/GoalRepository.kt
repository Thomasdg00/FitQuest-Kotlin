package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.local.dao.GoalDao
import com.univpm.fitquest.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    private val goalDao: GoalDao
) {
    fun observeGoals(): Flow<List<GoalEntity>> = goalDao.observeAll()

    suspend fun getGoalForSport(sport: String): GoalEntity? =
        goalDao.getForSport(sport)

    suspend fun saveGoal(goal: GoalEntity) =
        goalDao.upsert(goal)
}
