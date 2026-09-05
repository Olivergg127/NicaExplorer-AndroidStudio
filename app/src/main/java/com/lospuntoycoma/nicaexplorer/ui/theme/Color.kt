package com.lospuntoycoma.nicaexplorer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Paleta "Guardabarranco" — ave nacional de Nicaragua.
 * Verde turquesa del cuerpo, azul de la corona y terracota del pecho,
 * inspirada también en la cerámica de San Juan de Oriente y los
 * paisajes turísticos del país (lagunas, lagos y atardeceres).
 */

// Primaria: verde turquesa del guardabarranco (lagunas y reservas naturales)
val TurquoisePrimary = Color(0xFF00A08A)
val TurquoiseDark = Color(0xFF00695C)
val TurquoiseLight = Color(0xFF7FD9CF)
val TurquoiseSurface = Color(0xFFE0F4F1)

// Secundaria: azul corona del ave / cielo sobre el lago
val SkyPrimary = Color(0xFF4098D7)
val SkyDark = Color(0xFF175E92)
val SkyLight = Color(0xFF90C8EE)
val SkySurface = Color(0xFFE7F2FB)

// Terciaria: terracota del pecho rufo / cerámica nicaragüense
val CeramicTertiary = Color(0xFFC1553B)
val CeramicDark = Color(0xFF8A3A26)
val CeramicLight = Color(0xFFEDA48F)
val CeramicSurface = Color(0xFFFAEBE5)

// Verde exclusivo para elementos de WhatsApp
val GreenPrimary = Color(0xFF2E7D32)
val GreenLight = Color(0xFF81C784)
val GreenSurface = Color(0xFFE8F5E9)

// Acento dorado: sol nica / flor sacuanjoche
val GoldAccent = Color(0xFFFFB300)
val GoldLight = Color(0xFFFFE082)

val DarkBackground = Color(0xFF0F1413)
val DarkSurface = Color(0xFF151C19)
val DarkSurfaceVariant = Color(0xFF1E2824)

// Fondos claros con tibieza de arena volcánica del Pacífico
val LightBackground = Color(0xFFFDFAF4)
val LightSurface = Color(0xFFFFFDF8)
val LightSurfaceVariant = Color(0xFFF5EFE6)

// Degradado general de la app: carbón -> verde bosque profundo -> teal muy suave
val AppBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0D1211),
        Color(0xFF111916),
        Color(0xFF0E181B)
    )
)

// Barra de navegación inferior: verde bosque muy oscuro -> carbón
val BottomNavBarBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF16221D), Color(0xFF0F1613))
)

// Variantes claras de los mismos degradados para modo claro (arena cálida)
val LightAppBackgroundBrush = Brush.verticalGradient(
    colors = listOf(LightBackground, LightSurfaceVariant)
)

val LightBottomNavBarBrush = Brush.verticalGradient(
    colors = listOf(LightSurface, LightSurfaceVariant)
)

// Degradado de marca para encabezados sobre modo oscuro
val BrandHeaderBrush = Brush.horizontalGradient(colors = listOf(TurquoiseDark, SkyDark))

// Gradiente de marca: guardabarranco en vuelo (turquesa -> azul)
val GradientStart = Color(0xFF00A08A)
val GradientEnd = Color(0xFF4098D7)

val TextOnDark = Color(0xFFF5F0F5)
val TextOnLight = Color(0xFF1C1B1F)
val TextMuted = Color(0xFF939393)

val CardDark = Color(0xFF2A2A2A)
val CardLight = Color(0xFFFFFDF8)

val ErrorRed = Color(0xFFE53935)
val SuccessGreen = Color(0xFF43A047)
