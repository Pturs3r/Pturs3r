package com.pturs3r.carlauncher

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CarColors {
    val Background = Color(0xFF0B0F14)
    val Surface = Color(0xFF151B23)
    val SurfaceHigh = Color(0xFF1E2631)
    val Accent = Color(0xFF4FC3F7)
    val AccentWarm = Color(0xFFFFB74D)
    val TextPrimary = Color(0xFFECEFF4)
    val TextSecondary = Color(0xFF8A96A8)
    val Error = Color(0xFFEF5350)
}

private val scheme = darkColorScheme(
    primary = CarColors.Accent,
    onPrimary = CarColors.Background,
    secondary = CarColors.AccentWarm,
    background = CarColors.Background,
    onBackground = CarColors.TextPrimary,
    surface = CarColors.Surface,
    onSurface = CarColors.TextPrimary,
    surfaceVariant = CarColors.SurfaceHigh,
    onSurfaceVariant = CarColors.TextSecondary,
    error = CarColors.Error,
)

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
