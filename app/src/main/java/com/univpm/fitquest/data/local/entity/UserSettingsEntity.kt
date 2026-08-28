package com.univpm.fitquest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.univpm.fitquest.domain.model.ThemeMode

/**
 * Single-row app settings table. Keep id = 1 for the current user.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = DEFAULT_ID,
    val bodyWeightKg: Double = 70.0,
    val themeMode: String = ThemeMode.Light.storageValue,
) {
    companion object {
        const val DEFAULT_ID = 1
    }
}
