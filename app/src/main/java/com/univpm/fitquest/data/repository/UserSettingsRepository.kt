package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.local.dao.UserSettingsDao
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import com.univpm.fitquest.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UserSettingsRepository(
    private val userSettingsDao: UserSettingsDao
) {
    private val settingsMutex = Mutex()

    fun observeSettings(): Flow<UserSettingsEntity?> =
        userSettingsDao.observe()

    suspend fun getSettings(): UserSettingsEntity? =
        userSettingsDao.get()

    suspend fun ensureSettings() = settingsMutex.withLock {
        if (userSettingsDao.get() == null) {
            userSettingsDao.upsert(UserSettingsEntity())
        }
    }

    suspend fun updateBodyWeight(bodyWeightKg: Double) = updateSettings {
        it.copy(bodyWeightKg = bodyWeightKg)
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) = updateSettings {
        it.copy(themeMode = themeMode.storageValue)
    }

    private suspend fun updateSettings(
        update: (UserSettingsEntity) -> UserSettingsEntity,
    ) = settingsMutex.withLock {
        val current = userSettingsDao.get() ?: UserSettingsEntity()
        userSettingsDao.upsert(update(current))
    }
}
