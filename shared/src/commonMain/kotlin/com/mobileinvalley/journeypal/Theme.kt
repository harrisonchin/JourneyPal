package com.mobileinvalley.journeypal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00668B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC9E6FF),
    onPrimaryContainer = Color(0xFF001E2F),
    secondary = Color(0xFF4F606E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3E5F5),
    onSecondaryContainer = Color(0xFF0B1D29),
    tertiary = Color(0xFF64597C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEADBFF),
    onTertiaryContainer = Color(0xFF201635),
    background = Color(0xFFF8F9FF),
    surface = Color(0xFFF8F9FF),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF76D1FF),
    onPrimary = Color(0xFF00354A),
    primaryContainer = Color(0xFF004D69),
    onPrimaryContainer = Color(0xFFC9E6FF),
    secondary = Color(0xFFB7C9D9),
    onSecondary = Color(0xFF21323F),
    secondaryContainer = Color(0xFF384956),
    onSecondaryContainer = Color(0xFFD3E5F5),
    tertiary = Color(0xFFCEC0E8),
    onTertiary = Color(0xFF352B4B),
    tertiaryContainer = Color(0xFF4C4162),
    onTertiaryContainer = Color(0xFFEADBFF),
    background = Color(0xFF191C1E),
    surface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C7CE)
)

enum class ThemeMode {
    Light, Dark, System
}

val LocalThemeMode = compositionLocalOf { mutableStateOf(ThemeMode.System) }

@Composable
fun JourneyPalTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
