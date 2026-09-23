package com.nexora.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Neumorphism (soft-UI) palette, ported 1:1 from the reference
 * "Neumorphism Calculator" design: a background and every card/control share the
 * same base color, and depth comes only from a light shadow (top-left) paired
 * with a dark shadow (bottom-right) — see [com.nexora.player.ui.components.neumorphic].
 * Two full variants (Light / Dark) are exposed as [NexoraThemeVariant]; every
 * screen reads from [NexoraExtendedColors] instead of these raw values.
 */

// ---- Neumorphic Light ----
val NeuLightBase = Color(0xFFE0E5EC)
val NeuLightBaseElevated = Color(0xFFE9EEF5)
val NeuLightShadowLight = Color(0xFFFFFFFF)
val NeuLightShadowDark = Color(0xFFA3B1C6)
val NeuLightTextPrimary = Color(0xFF4D5B68)
val NeuLightTextSecondary = Color(0xFF7A8B9A)
val NeuLightTextTertiary = Color(0xFF98A6B8)
val NeuLightAccent = Color(0xFF0984E3)
val NeuLightAccentSecondary = Color(0xFF7A8B9A)
val NeuLightDanger = Color(0xFFD63031)
val NeuLightSuccess = Color(0xFF12B886)
val NeuLightWarning = Color(0xFFE8A23D)

// ---- Neumorphic Dark ----
val NeuDarkBase = Color(0xFF24252A)
val NeuDarkBaseElevated = Color(0xFF2A2B31)
val NeuDarkShadowLight = Color(0xFF2C2D33)
val NeuDarkShadowDark = Color(0xFF1C1D21)
val NeuDarkTextPrimary = Color(0xFFE2E8F0)
val NeuDarkTextSecondary = Color(0xFFA0AEC0)
val NeuDarkTextTertiary = Color(0xFF78839A)
val NeuDarkAccent = Color(0xFF4FACFE)
val NeuDarkAccentSecondary = Color(0xFFA0AEC0)
val NeuDarkDanger = Color(0xFFFF6B6B)
val NeuDarkSuccess = Color(0xFF3ECF8E)
val NeuDarkWarning = Color(0xFFE8A23D)

// ---- Shared overlays (drawn on top of video thumbnails/scrims, not the neumorphic
// chrome, so these stay fixed regardless of the light/dark variant) ----
val BadgeBackground = Color(0x99000000)
val ScrimStrong = Color(0xCC101216)
val ScrimSoft = Color(0x66101216)
