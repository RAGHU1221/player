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

/** Neumorphism (soft-UI) theme, persisted via DataStore (see [com.nexora.player.data.datastore.SettingsDataStore]). */
enum class NexoraThemeVariant(val displayName: String) {
    NEUMORPHIC_LIGHT("Light"),
    NEUMORPHIC_DARK("Dark"),
}

/**
 * Tokens that Material3's [androidx.compose.material3.ColorScheme] doesn't model
 * directly (the two neumorphic shadow tones, glow colors, status colors). Every
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
    /** Highlight shadow — sits toward the top-left of a raised neumorphic surface. */
    val shadowLight: Color,
    /** Umbra shadow — sits toward the bottom-right of a raised neumorphic surface. */
    val shadowDark: Color,
    val accent: Color,
    val accentDim: Color,
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

private fun neumorphicLight() = NexoraExtendedColors(
    isLight = true,
    gradientTop = NeuLightBase,
    gradientBottom = NeuLightBase,
    surface = NeuLightBase,
    surfaceElevated = NeuLightBaseElevated,
    glassBorder = NeuLightShadowDark.copy(alpha = 0.25f),
    glassBorderStrong = NeuLightShadowDark.copy(alpha = 0.4f),
    shadowLight = NeuLightShadowLight,
    shadowDark = NeuLightShadowDark,
    accent = NeuLightAccent,
    accentDim = NeuLightAccent.copy(alpha = 0.6f),
    accentGlow = NeuLightAccent.copy(alpha = 0.35f),
    accentSecondary = NeuLightAccentSecondary,
    danger = NeuLightDanger,
    statusActive = NeuLightSuccess,
    statusActiveGlow = NeuLightSuccess.copy(alpha = 0.35f),
    statusWarning = NeuLightWarning,
    textPrimary = NeuLightTextPrimary,
    textSecondary = NeuLightTextSecondary,
    textTertiary = NeuLightTextTertiary,
    divider = NeuLightShadowDark.copy(alpha = 0.4f),
    badgeBackground = BadgeBackground,
    scrimStrong = ScrimStrong,
    scrimSoft = ScrimSoft,
)

private fun neumorphicDark() = NexoraExtendedColors(
    isLight = false,
    gradientTop = NeuDarkBase,
    gradientBottom = NeuDarkBase,
    surface = NeuDarkBase,
    surfaceElevated = NeuDarkBaseElevated,
    glassBorder = NeuDarkShadowDark.copy(alpha = 0.6f),
    glassBorderStrong = NeuDarkShadowDark.copy(alpha = 0.8f),
    shadowLight = NeuDarkShadowLight,
    shadowDark = NeuDarkShadowDark,
    accent = NeuDarkAccent,
    accentDim = NeuDarkAccent.copy(alpha = 0.6f),
    accentGlow = NeuDarkAccent.copy(alpha = 0.35f),
    accentSecondary = NeuDarkAccentSecondary,
    danger = NeuDarkDanger,
    statusActive = NeuDarkSuccess,
    statusActiveGlow = NeuDarkSuccess.copy(alpha = 0.35f),
    statusWarning = NeuDarkWarning,
    textPrimary = NeuDarkTextPrimary,
    textSecondary = NeuDarkTextSecondary,
    textTertiary = NeuDarkTextTertiary,
    divider = NeuDarkShadowDark.copy(alpha = 0.6f),
    badgeBackground = BadgeBackground,
    scrimStrong = ScrimStrong,
    scrimSoft = ScrimSoft,
)

fun NexoraThemeVariant.toExtendedColors(): NexoraExtendedColors = when (this) {
    NexoraThemeVariant.NEUMORPHIC_LIGHT -> neumorphicLight()
    NexoraThemeVariant.NEUMORPHIC_DARK -> neumorphicDark()
}

val LocalNexoraColors = staticCompositionLocalOf { neumorphicDark() }

@Composable
fun NexoraTheme(
    variant: NexoraThemeVariant = NexoraThemeVariant.NEUMORPHIC_DARK,
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

/** Full-bleed flat backdrop used behind every top-level screen (same color as every
 * neumorphic card, per the soft-UI convention — depth comes from shadows, not contrast). */
fun Modifier.nexoraBackground(colors: NexoraExtendedColors): Modifier =
    this
        .fillMaxSize()
        .background(colors.backgroundBrush)
