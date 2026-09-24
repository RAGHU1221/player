package com.nexora.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * "Glow" theme palette — dark glass cards on a near-black backdrop, with a warm
 * orange → red sunset-gradient accent used for highlights, active states and
 * neon-ring glows (ported from the reference dashboard-card design: a frosted
 * glass panel, a glowing accent ring around the primary action, a warm gradient
 * line chart). Two full variants (Light / Dark) are exposed as
 * [NexoraThemeVariant]; every screen reads from [NexoraExtendedColors] instead
 * of these raw values.
 */

// ---- Glow Dark ----
val GlowDarkGradientTop = Color(0xFF14161D)
val GlowDarkGradientBottom = Color(0xFF07080B)
val GlowDarkSurface = Color(0xFF16181F)
val GlowDarkSurfaceElevated = Color(0xFF1D202A)
val GlowDarkBorder = Color(0x1FFFFFFF)
val GlowDarkBorderStrong = Color(0x33FFFFFF)
val GlowDarkShadow = Color(0xCC000000)
val GlowDarkTextPrimary = Color(0xFFF5F6FA)
val GlowDarkTextSecondary = Color(0xFF9BA1AE)
val GlowDarkTextTertiary = Color(0xFF6C7280)
val GlowDarkAccent = Color(0xFFFF6B35)
val GlowDarkAccentSecondary = Color(0xFFFF3D68)
val GlowDarkDanger = Color(0xFFFF4D5E)
val GlowDarkSuccess = Color(0xFF2ED573)
val GlowDarkWarning = Color(0xFFFFA502)

// ---- Glow Light ----
val GlowLightGradientTop = Color(0xFFF3F4F8)
val GlowLightGradientBottom = Color(0xFFE7E9F0)
val GlowLightSurface = Color(0xFFFFFFFF)
val GlowLightSurfaceElevated = Color(0xFFFAFAFD)
val GlowLightBorder = Color(0x14000000)
val GlowLightBorderStrong = Color(0x26000000)
val GlowLightShadow = Color(0x33000000)
val GlowLightTextPrimary = Color(0xFF1B1D24)
val GlowLightTextSecondary = Color(0xFF5B6270)
val GlowLightTextTertiary = Color(0xFF8A909C)
val GlowLightAccent = Color(0xFFF2542D)
val GlowLightAccentSecondary = Color(0xFFE8355F)
val GlowLightDanger = Color(0xFFE0293D)
val GlowLightSuccess = Color(0xFF17A75C)
val GlowLightWarning = Color(0xFFD97706)

// ---- Shared overlays (drawn on top of video thumbnails/scrims, not the theme
// chrome, so these stay fixed regardless of the light/dark variant) ----
val BadgeBackground = Color(0x99000000)
val ScrimStrong = Color(0xCC101216)
val ScrimSoft = Color(0x66101216)
