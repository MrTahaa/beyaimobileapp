package com.example.beyazai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BeyazTeal,
    onPrimary = BeyazNavy,
    primaryContainer = BeyazDarkSurfaceVariant,
    onPrimaryContainer = BeyazDarkText,
    secondary = BeyazSky,
    onSecondary = BeyazNavy,
    tertiary = BeyazWarning,
    background = BeyazDarkBackground,
    onBackground = BeyazDarkText,
    surface = BeyazDarkSurface,
    onSurface = BeyazDarkText,
    surfaceVariant = BeyazDarkSurfaceVariant,
    onSurfaceVariant = BeyazSurfaceVariant,
    error = BeyazError
)

private val LightColorScheme = lightColorScheme(
    primary = BeyazNavy,
    onPrimary = BeyazSurface,
    primaryContainer = BeyazSurfaceVariant,
    onPrimaryContainer = BeyazNavy,
    secondary = BeyazTeal,
    onSecondary = BeyazSurface,
    secondaryContainer = BeyazSuccessContainer,
    onSecondaryContainer = BeyazTealDark,
    tertiary = BeyazSky,
    background = BeyazBackground,
    onBackground = BeyazTextPrimary,
    surface = BeyazSurface,
    onSurface = BeyazTextPrimary,
    surfaceVariant = BeyazSurfaceVariant,
    onSurfaceVariant = BeyazTextSecondary,
    outline = BeyazDivider,
    error = BeyazError
)

@Composable
fun BeyazaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
