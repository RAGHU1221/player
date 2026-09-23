package com.nexora.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors

/**
 * The single reusable surface behind every card in the app: a raised neumorphic
 * ("soft-UI") panel — same base color as the page background, with a light
 * highlight and dark umbra shadow standing in for a border. Every list row,
 * stat tile and bottom sheet builds on this instead of Material's default Card,
 * which is how the whole app stays visually consistent with the neumorphism
 * reference design.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = NexoraRadius.Card,
    fillAlpha: Float = 1f,
    pressed: Boolean = false,
    @Suppress("UNUSED_PARAMETER") borderColor: Color = MaterialTheme.nexoraColors.glassBorder,
    containerColor: Color = MaterialTheme.nexoraColors.surface,
    content: @Composable () -> Unit,
) {
    // Neumorphic cards use a light/dark shadow pair (from .neumorphic) instead of
    // a hairline border for depth; borderColor is kept only for source
    // compatibility with existing call sites.
    Box(
        modifier = modifier
            .neumorphic(cornerRadius = cornerRadius, pressed = pressed)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor.copy(alpha = containerColor.alpha * fillAlpha)),
    ) {
        content()
    }
}
