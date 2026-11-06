package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrightDigitalColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1B1400),
    primaryContainer = GoldContainer,
    onPrimaryContainer = OnGoldContainer,
    secondary = GoldMetallic,
    onSecondary = Color(0xFF1B1400),
    secondaryContainer = Color(0xFF2B2414),
    onSecondaryContainer = GoldLight,
    tertiary = GoldAccent,
    onTertiary = Color.Black,
    background = BlackBg,
    onBackground = TextWhite,
    surface = BlackSurface,
    onSurface = TextWhite,
    surfaceVariant = BlackSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = BlackBorder,
    outlineVariant = Color(0xFF382F1C)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Forced to false to preserve the luxury Black & Gold branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BrightDigitalColorScheme,
        typography = Typography,
        content = content
    )
}
