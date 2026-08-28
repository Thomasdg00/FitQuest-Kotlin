package com.univpm.fitquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.univpm.fitquest.domain.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = TrailGreen,
    secondary = SummitBlue,
    background = WarmSand,
    surface = WarmSand,
    onBackground = Charcoal,
    onSurface = Charcoal,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError,
)

@Composable
fun FitQuestTheme(
    themeMode: ThemeMode = ThemeMode.Light,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode == ThemeMode.Dark

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
