package com.univpm.fitquest.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.univpm.fitquest.data.local.dao.GoalDao
import com.univpm.fitquest.data.local.dao.RoutePointDao
import com.univpm.fitquest.data.local.dao.UserSettingsDao
import com.univpm.fitquest.data.local.dao.WorkoutDao
import com.univpm.fitquest.data.local.entity.GoalEntity
import com.univpm.fitquest.data.local.entity.RoutePointEntity
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import com.univpm.fitquest.data.local.entity.WorkoutEntity

@Database(
    entities = [
        WorkoutEntity::class,
        RoutePointEntity::class,
        GoalEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FitQuestDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun routePointDao(): RoutePointDao
    abstract fun goalDao(): GoalDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: FitQuestDatabase? = null

        private const val DATABASE_NAME = "fitquest.db"

        fun getInstance(context: Context): FitQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FitQuestDatabase::class.java,
                    DATABASE_NAME,
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
