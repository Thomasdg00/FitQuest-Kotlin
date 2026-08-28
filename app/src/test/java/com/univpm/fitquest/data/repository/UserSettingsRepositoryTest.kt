package com.univpm.fitquest.data.repository

import com.univpm.fitquest.data.local.dao.UserSettingsDao
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import com.univpm.fitquest.domain.model.ThemeMode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UserSettingsRepositoryTest {
    @Test
    fun bodyWeightUpdatePreservesTheme() = runBlocking {
        val dao = FakeUserSettingsDao(settings(bodyWeightKg = 70.0, themeMode = ThemeMode.Dark))
        val repository = UserSettingsRepository(dao)

        repository.updateBodyWeight(75.0)

        assertEquals(settings(bodyWeightKg = 75.0, themeMode = ThemeMode.Dark), dao.settings)
    }

    @Test
    fun themeUpdatePreservesBodyWeight() = runBlocking {
        val dao = FakeUserSettingsDao(settings(bodyWeightKg = 75.0, themeMode = ThemeMode.Light))
        val repository = UserSettingsRepository(dao)

        repository.updateThemeMode(ThemeMode.Dark)

        assertEquals(settings(bodyWeightKg = 75.0, themeMode = ThemeMode.Dark), dao.settings)
    }

    @Test
    fun concurrentIndependentUpdatesPreserveBothValues() = runBlocking {
        val dao = FakeUserSettingsDao(settings(bodyWeightKg = 70.0, themeMode = ThemeMode.Light)).apply {
            blockNextUpsert = true
        }
        val repository = UserSettingsRepository(dao)

        val weightUpdate = async(start = CoroutineStart.UNDISPATCHED) {
            repository.updateBodyWeight(75.0)
        }
        dao.upsertStarted.await()
        val themeUpdate = async(start = CoroutineStart.UNDISPATCHED) {
            repository.updateThemeMode(ThemeMode.Dark)
        }
        dao.continueUpsert.complete(Unit)
        awaitAll(weightUpdate, themeUpdate)

        assertEquals(settings(bodyWeightKg = 75.0, themeMode = ThemeMode.Dark), dao.settings)
    }

    private fun settings(bodyWeightKg: Double, themeMode: ThemeMode) = UserSettingsEntity(
        bodyWeightKg = bodyWeightKg,
        themeMode = themeMode.storageValue,
    )

    private class FakeUserSettingsDao(initialSettings: UserSettingsEntity?) : UserSettingsDao {
        var settings: UserSettingsEntity? = initialSettings
            private set
        var blockNextUpsert = false
        val upsertStarted = CompletableDeferred<Unit>()
        val continueUpsert = CompletableDeferred<Unit>()

        override suspend fun upsert(settings: UserSettingsEntity) {
            if (blockNextUpsert) {
                blockNextUpsert = false
                upsertStarted.complete(Unit)
                continueUpsert.await()
            }
            this.settings = settings
        }

        override suspend fun get(settingsId: Int): UserSettingsEntity? = settings

        override fun observe(settingsId: Int): Flow<UserSettingsEntity?> = flowOf(settings)
    }
}
