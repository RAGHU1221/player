package com.nexora.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexora.player.domain.model.HdrType
import com.nexora.player.domain.model.Resolution
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors

@Composable
private fun BadgeChip(text: String, tint: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = tint,
        fontSize = 10.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        modifier = modifier
            .background(MaterialTheme.nexoraColors.badgeBackground, RoundedCornerShape(NexoraRadius.Badge))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun ResolutionBadge(resolution: Resolution, modifier: Modifier = Modifier) {
    val tint = if (resolution == Resolution.UHD_4K) MaterialTheme.nexoraColors.accent else Color.White
    BadgeChip(text = resolution.label, tint = tint, modifier = modifier)
}

@Composable
fun HdrBadge(hdrType: HdrType, modifier: Modifier = Modifier) {
    if (hdrType == HdrType.NONE) return
    BadgeChip(text = hdrType.label, tint = MaterialTheme.nexoraColors.statusActive, modifier = modifier)
}

@Composable
fun DurationBadge(text: String, modifier: Modifier = Modifier) {
    BadgeChip(text = text, tint = Color.White, modifier = modifier)
}

@Composable
fun QualityIndicatorRow(
    resolutionLabel: String,
    codec: String,
    hdrType: HdrType,
    fps: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        Icon(
            Icons.Filled.HighQuality,
            contentDescription = null,
            tint = MaterialTheme.nexoraColors.accent,
            modifier = Modifier.padding(end = 4.dp),
        )
        Text(
            text = listOf(resolutionLabel, codec, hdrType.takeIf { it != HdrType.NONE }?.label, fps)
                .filterNotNull().joinToString("  •  "),
            color = MaterialTheme.nexoraColors.textSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
