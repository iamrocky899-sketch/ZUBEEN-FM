package com.amairatech.zubeenfm.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ZubeenDarkColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianBackground,
    primaryContainer = DeepAmber,
    onPrimaryContainer = TextPureWhite,
    secondary = MugaGold,
    onSecondary = ObsidianBackground,
    secondaryContainer = ObsidianCardElevated,
    onSecondaryContainer = SoftGold,
    tertiary = AmberGlow,
    onTertiary = ObsidianBackground,
    background = ObsidianBackground,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    error = LiveCrimson
)

@Composable
fun ZubeenFMTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZubeenDarkColorScheme,
        typography = Typography,
        content = content
    )
}