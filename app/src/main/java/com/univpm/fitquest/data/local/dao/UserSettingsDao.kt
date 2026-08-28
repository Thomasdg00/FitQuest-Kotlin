package com.univpm.fitquest.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Upsert
    suspend fun upsert(settings: UserSettingsEntity)

    @Query("SELECT * FROM user_settings WHERE id = :settingsId")
    suspend fun get(settingsId: Int = UserSettingsEntity.DEFAULT_ID): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = :settingsId")
    fun observe(settingsId: Int = UserSettingsEntity.DEFAULT_ID): Flow<UserSettingsEntity?>
}
