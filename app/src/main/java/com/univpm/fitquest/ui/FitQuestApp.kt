package com.univpm.fitquest.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.univpm.fitquest.data.local.entity.UserSettingsEntity
import com.univpm.fitquest.di.AppContainer
import com.univpm.fitquest.domain.model.ThemeMode
import com.univpm.fitquest.ui.navigation.FitQuestNavHost
import com.univpm.fitquest.ui.theme.FitQuestTheme

@Composable
fun FitQuestApp(appContainer: AppContainer) {
    val settings by appContainer.userSettingsRepository.observeSettings().collectAsState(initial = null)
    val resolvedSettings = settings ?: UserSettingsEntity()
    val themeMode = ThemeMode.fromStorageValue(resolvedSettings.themeMode)
    FitQuestTheme(themeMode = themeMode) {
        FitQuestNavHost(appContainer = appContainer)
    }
}
