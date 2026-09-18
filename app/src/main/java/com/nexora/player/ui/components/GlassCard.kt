package com.nexora.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors

/**
 * The single reusable surface behind every card in the app — dark translucent
 * navy, a hairline border, soft corner radius. Every list row, stat tile and
 * bottom sheet builds on this instead of Material's default Card, which is how
 * the whole app stays visually consistent with the reference design.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = NexoraRadius.Card,
    fillAlpha: Float = 1f,
    borderColor: Color = MaterialTheme.nexoraColors.glassBorder,
    containerColor: Color = MaterialTheme.nexoraColors.surface,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor.copy(alpha = containerColor.alpha * fillAlpha))
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius)),
    ) {
        content()
    }
}
