package com.nexora.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette distilled from the Nexora reference design: a soft slate-blue gradient
 * backdrop, near-black navy glass cards, a single confident red accent, and a
 * green pulse reserved for "live / active" states. Every theme in [NexoraTheme]
 * remixes these tokens rather than introducing new hues, so the app always reads
 * as one system.
 */

// ---- Navy Glass (default) ----
val NavyGradientTop = Color(0xFF5B6B99)
val NavyGradientBottom = Color(0xFF2C3350)
val NavyGlassSurface = Color(0xFF141A2E)
val NavyGlassSurfaceElevated = Color(0xFF1B2238)
val NavyGlassBorder = Color(0x1FFFFFFF)
val NavyGlassBorderStrong = Color(0x33FFFFFF)

// ---- Dark AMOLED ----
val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0D0D0F)
val AmoledSurfaceElevated = Color(0xFF16161A)

// ---- Midnight Blue ----
val MidnightGradientTop = Color(0xFF223055)
val MidnightGradientBottom = Color(0xFF0B0F1F)
val MidnightSurface = Color(0xFF10152A)
val MidnightSurfaceElevated = Color(0xFF171E38)

// ---- Red Premium ----
val RedPremiumGradientTop = Color(0xFF3A1620)
val RedPremiumGradientBottom = Color(0xFF160A12)
val RedPremiumSurface = Color(0xFF1A0E14)
val RedPremiumSurfaceElevated = Color(0xFF231420)

// ---- Shared accents ----
val AccentRed = Color(0xFFE8443D)
val AccentRedDim = Color(0xFF9C2E2A)
val AccentRedGlow = Color(0x66E8443D)
val AccentGreen = Color(0xFF3ECF8E)
val AccentGreenGlow = Color(0x553ECF8E)
val AccentAmber = Color(0xFFE8A23D)

val TextPrimary = Color(0xFFF5F6FA)
val TextSecondary = Color(0xFFA6ADC4)
val TextTertiary = Color(0xFF6E7690)
val Divider = Color(0x14FFFFFF)

val BadgeBackground = Color(0x99000000)
val ScrimStrong = Color(0xCC0A0D1A)
val ScrimSoft = Color(0x660A0D1A)
