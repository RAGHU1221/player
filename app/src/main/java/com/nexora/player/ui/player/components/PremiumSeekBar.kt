package com.nexora.player.ui.player.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.videoFrameMillis
import com.nexora.player.data.media.ThumbnailProvider
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.FormatUtils

/**
 * A custom scrub bar (spec section 11): current / remaining / total time,
 * a buffered-position track, smooth drag-to-seek, and a live thumbnail preview
 * that follows the thumb while scrubbing.
 */
@Composable
fun PremiumSeekBar(
    positionMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    videoUri: String,
    onSeekPreview: (Long) -> Unit,
    onSeekCommit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.nexoraColors
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1)
    val currentFraction = if (isDragging) dragFraction else (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val bufferedFraction = (bufferedMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val scrubPositionMs = (currentFraction * safeDuration).toLong()

    Column(modifier = modifier) {
        if (isDragging) {
            ScrubPreview(videoUri = videoUri, positionMs = scrubPositionMs, fraction = currentFraction)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat() }
                .pointerInput(durationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragFraction = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            dragFraction = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                            onSeekPreview((dragFraction * safeDuration).toLong())
                        },
                        onDragEnd = {
                            onSeekCommit((dragFraction * safeDuration).toLong())
                            isDragging = false
                        },
                        onDragCancel = { isDragging = false },
                    )
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                val trackY = size.height / 2
                drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, trackY), Offset(size.width, trackY), strokeWidth = size.height)
                drawLine(
                    Color.White.copy(alpha = 0.35f),
                    Offset(0f, trackY),
                    Offset(size.width * bufferedFraction, trackY),
                    strokeWidth = size.height,
                )
                drawLine(
                    colors.accent,
                    Offset(0f, trackY),
                    Offset(size.width * currentFraction, trackY),
                    strokeWidth = size.height,
                )
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset((trackWidthPx * currentFraction).toInt() - 7, 0) }
                    .size(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.accent),
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(FormatUtils.formatDuration(scrubPositionMs), color = colors.textPrimary, style = MaterialTheme.typography.labelMedium)
            Text(FormatUtils.remainingTime(scrubPositionMs, durationMs), color = colors.textSecondary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ScrubPreview(videoUri: String, positionMs: Long, fraction: Float) {
    val colors = MaterialTheme.nexoraColors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Column(
            modifier = Modifier
                .padding(start = (fraction * 240).dp.coerceIn(0.dp, 200.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceElevated)
                .padding(4.dp),
        ) {
            AsyncImage(
                model = ThumbnailProvider.requestFor(LocalContext.current, android.net.Uri.parse(videoUri))
                    .newBuilder().videoFrameMillis(positionMs).build(),
                contentDescription = null,
                modifier = Modifier.width(120.dp).height(68.dp).clip(RoundedCornerShape(6.dp)),
            )
            Text(
                FormatUtils.formatDuration(positionMs),
                color = colors.textPrimary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
