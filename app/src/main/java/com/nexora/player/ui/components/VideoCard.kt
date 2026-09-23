package com.nexora.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexora.player.data.media.ThumbnailProvider
import com.nexora.player.domain.model.Video
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.FormatUtils

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VideoCard(
    video: Video,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val colors = MaterialTheme.nexoraColors
    Column(
        modifier = modifier
            .neumorphic(cornerRadius = NexoraRadius.Card)
            .clip(RoundedCornerShape(NexoraRadius.Card))
            .background(colors.surface)
            .combinedClickable(onClick = onClick, onLongClick = onMoreClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        ) {
            AsyncImage(
                model = ThumbnailProvider.requestFor(androidx.compose.ui.platform.LocalContext.current, android.net.Uri.parse(video.uri)),
                contentDescription = video.filename,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Bottom scrim so white text/badges stay legible over any thumbnail
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, colors.scrimStrong), startY = 60f)),
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
            ) {
                ResolutionBadge(video.resolution)
                if (video.hdrType.label != "SDR") {
                    androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                    HdrBadge(video.hdrType)
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp),
            ) {
                Icon(
                    imageVector = if (video.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (video.isFavorite) colors.accent else Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            DurationBadge(
                text = FormatUtils.formatDuration(video.durationMs),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
            )

            val progress = video.watchProgress?.watchedPercent ?: 0f
            if (progress > 0.02f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.accent,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.filename,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(2.dp))
                Text(
                    text = listOf(
                        FormatUtils.formatFileSize(video.sizeBytes),
                        video.watchProgress?.let { "Watched ${(it.watchedPercent * 100).toInt()}%" },
                    ).filterNotNull().joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onMoreClick, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = colors.textSecondary)
            }
        }
    }
}
