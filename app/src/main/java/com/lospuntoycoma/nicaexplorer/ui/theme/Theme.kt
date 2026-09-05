package com.lospuntoycoma.nicaexplorer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = TurquoisePrimary,
    onPrimary = Color.White,
    primaryContainer = TurquoiseDark,
    onPrimaryContainer = TurquoiseLight,
    secondary = SkyPrimary,
    onSecondary = Color.White,
    secondaryContainer = SkyDark,
    onSecondaryContainer = SkyLight,
    tertiary = CeramicTertiary,
    onTertiary = Color.White,
    tertiaryContainer = CeramicDark,
    onTertiaryContainer = CeramicLight,
    error = ErrorRed,
    background = DarkBackground,
    onBackground = TextOnDark,
    surface = DarkSurface,
    onSurface = TextOnDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = TurquoisePrimary,
    onPrimary = Color.White,
    primaryContainer = TurquoiseSurface,
    onPrimaryContainer = TurquoiseDark,
    secondary = SkyPrimary,
    onSecondary = Color.White,
    secondaryContainer = SkySurface,
    onSecondaryContainer = SkyDark,
    tertiary = CeramicTertiary,
    onTertiary = Color.White,
    tertiaryContainer = CeramicSurface,
    onTertiaryContainer = CeramicDark,
    error = ErrorRed,
    background = LightBackground,
    onBackground = TextOnLight,
    surface = LightSurface,
    onSurface = TextOnLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun NicaExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Degradado de fondo según el tema activo: oscuro (carbón/verde) o claro
 * (arena cálida). Sustituye el uso directo de [AppBackgroundBrush], que solo
 * sirve para modo oscuro.
 */
@Composable
fun nicaAppBackgroundBrush(): Brush =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        AppBackgroundBrush
    } else {
        LightAppBackgroundBrush
    }

/**
 * Degradado de la barra inferior según el tema activo.
 */
@Composable
fun nicaBottomNavBarBrush(): Brush =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) {
        BottomNavBarBrush
    } else {
        LightBottomNavBarBrush
    }
