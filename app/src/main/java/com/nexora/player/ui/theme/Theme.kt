package com.nexora.player.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** "Glow" theme (dark glass + neon accent), persisted via DataStore (see [com.nexora.player.data.datastore.SettingsDataStore]). */
enum class NexoraThemeVariant(val displayName: String) {
    GLOW_LIGHT("Light"),
    GLOW_DARK("Dark"),
}

/**
 * Tokens that Material3's [androidx.compose.material3.ColorScheme] doesn't model
 * directly (glass border/shadow tones, the accent glow, status colors). Every
 * screen, card, control and dialog reads from this local instead of hard-coded
 * colors so switching [NexoraThemeVariant] restyles the whole app at once.
 */
data class NexoraExtendedColors(
    val isLight: Boolean,
    val gradientTop: Color,
    val gradientBottom: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassBorder: Color,
    val glassBorderStrong: Color,
    /** Frosted glass rim highlight drawn around raised surfaces. */
    val shadowLight: Color,
    /** Ambient drop-shadow color a raised surface casts on the backdrop. */
    val shadowDark: Color,
    val accent: Color,
    val accentDim: Color,
    /** Soft, wide-blurred accent color used for the neon glow behind active controls. */
    val accentGlow: Color,
    /** Muted secondary accent (used for less prominent selected/active controls). */
    val accentSecondary: Color,
    /** Reserved for destructive actions (delete, discard) — kept separate from [accent]. */
    val danger: Color,
    val statusActive: Color,
    val statusActiveGlow: Color,
    val statusWarning: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val badgeBackground: Color,
    val scrimStrong: Color,
    val scrimSoft: Color,
) {
    val backgroundBrush: Brush
        get() = Brush.verticalGradient(listOf(gradientTop, gradientBottom))
}

private fun glowLight() = NexoraExtendedColors(
    isLight = true,
    gradientTop = GlowLightGradientTop,
    gradientBottom = GlowLightGradientBottom,
    surface = GlowLightSurface,
    surfaceElevated = GlowLightSurfaceElevated,
    glassBorder = GlowLightBorder,
    glassBorderStrong = GlowLightBorderStrong,
    shadowLight = GlowLightBorder,
    shadowDark = GlowLightShadow,
    accent = GlowLightAccent,
    accentDim = GlowLightAccent.copy(alpha = 0.6f),
    accentGlow = GlowLightAccent.copy(alpha = 0.35f),
    accentSecondary = GlowLightAccentSecondary,
    danger = GlowLightDanger,
    statusActive = GlowLightSuccess,
    statusActiveGlow = GlowLightSuccess.copy(alpha = 0.35f),
    statusWarning = GlowLightWarning,
    textPrimary = GlowLightTextPrimary,
    textSecondary = GlowLightTextSecondary,
    textTertiary = GlowLightTextTertiary,
    divider = GlowLightBorderStrong,
    badgeBackground = BadgeBackground,
    scrimStrong = ScrimStrong,
    scrimSoft = ScrimSoft,
)

private fun glowDark() = NexoraExtendedColors(
    isLight = false,
    gradientTop = GlowDarkGradientTop,
    gradientBottom = GlowDarkGradientBottom,
    surface = GlowDarkSurface,
    surfaceElevated = GlowDarkSurfaceElevated,
    glassBorder = GlowDarkBorder,
    glassBorderStrong = GlowDarkBorderStrong,
    shadowLight = GlowDarkBorder,
    shadowDark = GlowDarkShadow,
    accent = GlowDarkAccent,
    accentDim = GlowDarkAccent.copy(alpha = 0.6f),
    accentGlow = GlowDarkAccent.copy(alpha = 0.45f),
    accentSecondary = GlowDarkAccentSecondary,
    danger = GlowDarkDanger,
    statusActive = GlowDarkSuccess,
    statusActiveGlow = GlowDarkSuccess.copy(alpha = 0.4f),
    statusWarning = GlowDarkWarning,
    textPrimary = GlowDarkTextPrimary,
    textSecondary = GlowDarkTextSecondary,
    textTertiary = GlowDarkTextTertiary,
    divider = GlowDarkBorderStrong,
    badgeBackground = BadgeBackground,
    scrimStrong = ScrimStrong,
    scrimSoft = ScrimSoft,
)

fun NexoraThemeVariant.toExtendedColors(): NexoraExtendedColors = when (this) {
    NexoraThemeVariant.GLOW_LIGHT -> glowLight()
    NexoraThemeVariant.GLOW_DARK -> glowDark()
}

val LocalNexoraColors = staticCompositionLocalOf { glowDark() }

@Composable
fun NexoraTheme(
    variant: NexoraThemeVariant = NexoraThemeVariant.GLOW_DARK,
    content: @Composable () -> Unit,
) {
    val extended = variant.toExtendedColors()

    val materialColorScheme = if (extended.isLight) {
        lightColorScheme(
            primary = extended.accent,
            onPrimary = Color.White,
            secondary = extended.statusActive,
            onSecondary = Color.White,
            background = extended.gradientBottom,
            onBackground = extended.textPrimary,
            surface = extended.surface,
            onSurface = extended.textPrimary,
            surfaceVariant = extended.surfaceElevated,
            onSurfaceVariant = extended.textSecondary,
            outline = extended.shadowDark,
            error = extended.danger,
            onError = Color.White,
        )
    } else {
        darkColorScheme(
            primary = extended.accent,
            onPrimary = Color.White,
            secondary = extended.statusActive,
            onSecondary = Color.Black,
            background = extended.gradientBottom,
            onBackground = extended.textPrimary,
            surface = extended.surface,
            onSurface = extended.textPrimary,
            surfaceVariant = extended.surfaceElevated,
            onSurfaceVariant = extended.textSecondary,
            outline = extended.shadowDark,
            error = extended.danger,
            onError = Color.White,
        )
    }

    CompositionLocalProvider(LocalNexoraColors provides extended) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = NexoraTypography,
            shapes = NexoraShapes,
            content = content,
        )
    }
}

/** Convenience accessor: `NexoraColors.accent` etc. from any `@Composable`. */
val MaterialTheme.nexoraColors: NexoraExtendedColors
    @Composable
    get() = LocalNexoraColors.current

/** Full-bleed backdrop used behind every top-level screen — a subtle vertical
 * gradient from [NexoraExtendedColors.gradientTop] into
 * [NexoraExtendedColors.gradientBottom], giving the glass cards drawn on top of
 * it some depth to sit in. */
fun Modifier.nexoraBackground(colors: NexoraExtendedColors): Modifier =
    this
        .fillMaxSize()
        .background(colors.backgroundBrush)
