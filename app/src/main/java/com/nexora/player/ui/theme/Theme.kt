package com.nexora.player.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** The four themes required by the spec. Persisted via DataStore (see [com.nexora.player.data.datastore.SettingsDataStore]). */
enum class NexoraThemeVariant(val displayName: String) {
    NAVY_GLASS("Navy Glass"),
    DARK_AMOLED("Dark AMOLED"),
    MIDNIGHT_BLUE("Midnight Blue"),
    RED_PREMIUM("Red Premium"),
}

/**
 * Tokens that Material3's [androidx.compose.material3.ColorScheme] doesn't model
 * directly (gradient stops, glass borders, glow colors, status colors). Every
 * screen, card, control and dialog reads from this local instead of hard-coded
 * colors so switching [NexoraThemeVariant] restyles the whole app at once.
 */
data class NexoraExtendedColors(
    val gradientTop: Color,
    val gradientBottom: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassBorder: Color,
    val glassBorderStrong: Color,
    val accent: Color,
    val accentDim: Color,
    val accentGlow: Color,
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

private fun navyGlass() = NexoraExtendedColors(
    gradientTop = NavyGradientTop,
    gradientBottom = NavyGradientBottom,
    surface = NavyGlassSurface,
    surfaceElevated = NavyGlassSurfaceElevated,
    glassBorder = NavyGlassBorder,
    glassBorderStrong = NavyGlassBorderStrong,
    accent = AccentRed,
    accentDim = AccentRedDim,
    accentGlow = AccentRedGlow,
    statusActive = AccentGreen,
    statusActiveGlow = AccentGreenGlow,
    statusWarning = AccentAmber,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    divider = Divider,
    badgeBackground = BadgeBackground,
    scrimStrong = ScrimStrong,
    scrimSoft = ScrimSoft,
)

private fun darkAmoled() = navyGlass().copy(
    gradientTop = Color(0xFF1A1A1F),
    gradientBottom = AmoledBackground,
    surface = AmoledSurface,
    surfaceElevated = AmoledSurfaceElevated,
)

private fun midnightBlue() = navyGlass().copy(
    gradientTop = MidnightGradientTop,
    gradientBottom = MidnightGradientBottom,
    surface = MidnightSurface,
    surfaceElevated = MidnightSurfaceElevated,
)

private fun redPremium() = navyGlass().copy(
    gradientTop = RedPremiumGradientTop,
    gradientBottom = RedPremiumGradientBottom,
    surface = RedPremiumSurface,
    surfaceElevated = RedPremiumSurfaceElevated,
    accent = AccentRed,
)

fun NexoraThemeVariant.toExtendedColors(): NexoraExtendedColors = when (this) {
    NexoraThemeVariant.NAVY_GLASS -> navyGlass()
    NexoraThemeVariant.DARK_AMOLED -> darkAmoled()
    NexoraThemeVariant.MIDNIGHT_BLUE -> midnightBlue()
    NexoraThemeVariant.RED_PREMIUM -> redPremium()
}

val LocalNexoraColors = staticCompositionLocalOf { navyGlass() }

@Composable
fun NexoraTheme(
    variant: NexoraThemeVariant = NexoraThemeVariant.NAVY_GLASS,
    content: @Composable () -> Unit,
) {
    val extended = variant.toExtendedColors()

    val materialColorScheme = darkColorScheme(
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
        outline = extended.glassBorder,
        error = AccentRed,
        onError = Color.White,
    )

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

/** Full-bleed gradient backdrop used behind every top-level screen. */
fun Modifier.nexoraBackground(colors: NexoraExtendedColors): Modifier =
    this
        .fillMaxSize()
        .background(colors.backgroundBrush)
