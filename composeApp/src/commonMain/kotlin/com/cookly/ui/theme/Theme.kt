package com.cookly.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Tokens de Cookly: los mismos colores de la versión iOS (Theme.swift).
 * El texto sobre el degradado usa tinta fija #111111, porque el blanco sobre #2FBDA8 no tiene contraste.
 */
object CooklyColors {
    val Primary = Color(0xFF188489)
    val Secondary = Color(0xFF2FBDA8)
    val OnGradient = Color(0xFF111111)
    val ToastBackground = Color(0xFF1A1A1A)

    /** Degradado de marca: de #2FBDA8 (arriba a la izquierda) a #188489 (abajo a la derecha). */
    val brandGradient = Brush.linearGradient(listOf(Secondary, Primary))
}

/** Colores que cambian entre claro y oscuro y que Material no trae (estados de vencimiento, tinta, etc.). */
@Immutable
data class CooklyPalette(
    val ink: Color,
    val brandForeground: Color,
    val canvas: Color,
    val surface: Color,
    val danger: Color,
    val dangerSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val success: Color,
    val successSoft: Color,
)

private val LightPalette = CooklyPalette(
    ink = Color(0xFF111111),
    brandForeground = Color(0xFF146E72),
    canvas = Color(0xFFF3F7F6),
    surface = Color(0xFFFFFFFF),
    danger = Color(0xFFB42318),
    dangerSoft = Color(0xFFFDECEA),
    warning = Color(0xFF8A5A00),
    warningSoft = Color(0xFFFFF3DC),
    success = Color(0xFF0E7A45),
    successSoft = Color(0xFFE5F6EC),
)

private val DarkPalette = CooklyPalette(
    ink = Color(0xFFF4F7F7),
    brandForeground = Color(0xFF7AD9CF),
    canvas = Color(0xFF101615),
    surface = Color(0xFF1C2624),
    danger = Color(0xFFFFB4AB),
    dangerSoft = Color(0xFF3B2220),
    warning = Color(0xFFFFD18A),
    warningSoft = Color(0xFF3A2E1A),
    success = Color(0xFF8EE0B0),
    successSoft = Color(0xFF173328),
)

/** Acceso a la paleta desde cualquier pantalla: LocalCookly.current.ink */
val LocalCookly = staticCompositionLocalOf { LightPalette }

private fun lightScheme(p: CooklyPalette) = lightColorScheme(
    primary = CooklyColors.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EFEC),
    onPrimaryContainer = p.brandForeground,
    secondary = CooklyColors.Secondary,
    onSecondary = CooklyColors.OnGradient,
    secondaryContainer = Color(0xFFD5EFEC),
    onSecondaryContainer = p.brandForeground,
    tertiary = CooklyColors.Secondary,
    background = p.canvas,
    onBackground = p.ink,
    surface = p.surface,
    onSurface = p.ink,
    surfaceVariant = Color(0xFFEAF1EF),
    onSurfaceVariant = Color(0xFF5E6B6A),
    surfaceContainer = p.surface,
    surfaceContainerHigh = p.surface,
    surfaceContainerHighest = Color(0xFFEAF1EF),
    surfaceContainerLow = p.surface,
    outline = Color(0xFFCAD5D4),
    outlineVariant = Color(0xFFE2E9E8),
    error = p.danger,
)

private fun darkScheme(p: CooklyPalette) = darkColorScheme(
    primary = Color(0xFF7AD9CF),
    onPrimary = CooklyColors.OnGradient,
    primaryContainer = Color(0xFF16504C),
    onPrimaryContainer = p.brandForeground,
    secondary = CooklyColors.Secondary,
    onSecondary = CooklyColors.OnGradient,
    secondaryContainer = Color(0xFF16504C),
    onSecondaryContainer = p.brandForeground,
    tertiary = CooklyColors.Secondary,
    background = p.canvas,
    onBackground = p.ink,
    surface = p.surface,
    onSurface = p.ink,
    surfaceVariant = Color(0xFF26322F),
    onSurfaceVariant = Color(0xFFA9B6B5),
    surfaceContainer = p.surface,
    surfaceContainerHigh = p.surface,
    surfaceContainerHighest = Color(0xFF26322F),
    surfaceContainerLow = p.surface,
    outline = Color(0xFF3A4847),
    outlineVariant = Color(0xFF2C3836),
    error = p.danger,
)

private val CooklyShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** Tema compartido: el mismo en Android y en Desktop. El modo oscuro viene de las preferencias. */
@Composable
fun CooklyTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    CompositionLocalProvider(LocalCookly provides palette) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme(palette) else lightScheme(palette),
            shapes = CooklyShapes,
            content = content,
        )
    }
}
