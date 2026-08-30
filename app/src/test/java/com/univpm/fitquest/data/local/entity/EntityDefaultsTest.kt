package com.univpm.fitquest.data.local.entity

import com.univpm.fitquest.domain.model.Sport
import com.univpm.fitquest.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityDefaultsTest {
    @Test
    fun newWorkoutStartsWithoutDatabaseId() {
        val workout = WorkoutEntity(sport = Sport.Running.routeValue, startedAtMillis = 1_000L)
        assertEquals(0L, workout.id)
        assertEquals(0L, workout.durationMillis)
        assertEquals(false, workout.isCompleted)
    }

    @Test
    fun defaultUserSettingsContainOnlyFinalDefaults() {
        val settings = UserSettingsEntity()
        assertEquals(UserSettingsEntity.DEFAULT_ID, settings.id)
        assertEquals(70.0, settings.bodyWeightKg, 0.0)
        assertEquals(ThemeMode.Light.storageValue, settings.themeMode)
    }
}
