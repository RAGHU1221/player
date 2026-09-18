package com.nexora.player.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NexoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

// Named radii used directly by bespoke components (cards, sheets, badges).
object NexoraRadius {
    val Badge = 8.dp
    val Card = 20.dp
    val CardLarge = 24.dp
    val Sheet = 28.dp
    val Pill = 100.dp
}
