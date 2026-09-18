package com.nexora.player.ui.miniplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexora.player.data.media.ThumbnailProvider
import com.nexora.player.player.PlayerUiState
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors

/**
 * Shown at the bottom of every top-level screen while a video plays in the
 * background (section 25). Tapping it re-opens the full player; it shares the
 * same [com.nexora.player.player.PlayerController] instance, so nothing is
 * re-created — playback continues exactly where it was.
 */
@Composable
fun MiniPlayerBar(
    videoUri: String,
    title: String,
    playback: PlayerUiState,
    onPlayPause: () -> Unit,
    onClose: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.nexoraColors
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onExpand),
        cornerRadius = NexoraRadius.Card,
        containerColor = colors.surfaceElevated,
    ) {
        androidx.compose.foundation.layout.Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = ThumbnailProvider.requestFor(androidx.compose.ui.platform.LocalContext.current, android.net.Uri.parse(videoUri)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
                Text(
                    title,
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                )
                IconButton(onClick = onPlayPause) {
                    Icon(
                        if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = colors.textPrimary,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }
            val progress = if (playback.durationMs > 0) (playback.positionMs.toFloat() / playback.durationMs) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = colors.accent,
                trackColor = colors.divider,
            )
        }
    }
}
