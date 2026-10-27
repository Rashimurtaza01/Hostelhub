package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = NavyDark,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = TealContainer,
    secondary = AmberAccent,
    onSecondary = NavyDark,
    secondaryContainer = NavySurface,
    onSecondaryContainer = CardSurface,
    background = NavyDark,
    onBackground = CardSurface,
    surface = NavyPrimary,
    onSurface = CardSurface,
    surfaceVariant = NavySurface,
    onSurfaceVariant = SlateBorder,
    outline = SlateMuted
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = Color.White,
    secondary = TealPrimary,
    onSecondary = Color.White,
    secondaryContainer = TealContainer,
    onSecondaryContainer = OnTealContainer,
    tertiary = AmberAccent,
    onTertiary = NavyDark,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = OffWhite,
    onBackground = SlateDark,
    surface = CardSurface,
    onSurface = SlateDark,
    surfaceVariant = SlateLight,
    onSurfaceVariant = SlateMuted,
    outline = SlateBorder
)

@Composable
fun HostelHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
