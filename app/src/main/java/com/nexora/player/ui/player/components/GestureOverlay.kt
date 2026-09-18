package com.nexora.player.ui.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nexora.player.ui.theme.nexoraColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private enum class DragAxis { NONE, HORIZONTAL_SEEK, VERTICAL_BRIGHTNESS, VERTICAL_VOLUME }

/**
 * Full-surface gesture layer (spec section 10): double-tap left/right to seek
 * ±10s, double-tap center to play/pause, vertical drag on the left half for
 * brightness / right half for volume, horizontal drag anywhere to scrub, pinch
 * to zoom, long-press for a temporary 2x speed boost, single tap to toggle the
 * controls overlay. Every gesture routes through callbacks so [PlayerScreen]
 * stays the single source of truth for player state.
 */
@Composable
fun GestureOverlay(
    enabled: Boolean,
    currentBrightness: Float,
    currentVolume: Float,
    doubleTapSeekSeconds: Int,
    onSingleTap: () -> Unit,
    onDoubleTapSeek: (forward: Boolean) -> Unit,
    onDoubleTapCenter: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSeekDrag: (deltaMs: Long) -> Unit,
    onSeekDragCommit: () -> Unit,
    onZoom: (Float) -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf<GestureFeedback?>(null) }
    var dragAxis by remember { mutableStateOf(DragAxis.NONE) }
    var accumulatedSeekMs by remember { mutableFloatStateOf(0f) }

    fun showFeedback(f: GestureFeedback, autoHideMs: Long = 700) {
        feedback = f
        scope.launch {
            delay(autoHideMs)
            if (feedback === f) feedback = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = { offset ->
                        if (offset.x < size.width / 3f) {
                            onDoubleTapSeek(false)
                            showFeedback(GestureFeedback.SeekBackward)
                        } else if (offset.x > size.width * 2f / 3f) {
                            onDoubleTapSeek(true)
                            showFeedback(GestureFeedback.SeekForward)
                        } else {
                            onDoubleTapCenter()
                        }
                    },
                    onPress = {
                        var boosting = false
                        val longPressJob = scope.launch {
                            delay(450)
                            boosting = true
                            onLongPressStart()
                            showFeedback(GestureFeedback.SpeedBoost, autoHideMs = 5000)
                        }
                        tryAwaitRelease()
                        longPressJob.cancel()
                        if (boosting) {
                            onLongPressEnd()
                            feedback = null
                        }
                    },
                )
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) onZoom(zoom)
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                androidx.compose.foundation.gestures.detectDragGestures(
                    onDragStart = { dragAxis = DragAxis.NONE; accumulatedSeekMs = 0f },
                    onDragEnd = {
                        if (dragAxis == DragAxis.HORIZONTAL_SEEK) onSeekDragCommit()
                        dragAxis = DragAxis.NONE
                        feedback = null
                    },
                    onDragCancel = { dragAxis = DragAxis.NONE; feedback = null },
                    onDrag = { change, dragAmount ->
                        if (dragAxis == DragAxis.NONE) {
                            dragAxis = if (abs(dragAmount.x) > abs(dragAmount.y) * 1.2f) {
                                DragAxis.HORIZONTAL_SEEK
                            } else if (change.position.x < size.width / 2f) {
                                DragAxis.VERTICAL_BRIGHTNESS
                            } else {
                                DragAxis.VERTICAL_VOLUME
                            }
                        }
                        when (dragAxis) {
                            DragAxis.HORIZONTAL_SEEK -> {
                                // Full screen width swipe == 90 seconds of seek — tuned to feel precise, not twitchy.
                                val deltaMs = (dragAmount.x / size.width) * 90_000L
                                accumulatedSeekMs += deltaMs
                                onSeekDrag(accumulatedSeekMs.toLong())
                                showFeedback(GestureFeedback.Seeking(accumulatedSeekMs.toLong()), autoHideMs = 2000)
                            }
                            DragAxis.VERTICAL_BRIGHTNESS -> {
                                val next = (currentBrightness - dragAmount.y / size.height).coerceIn(0f, 1f)
                                onBrightnessChange(next)
                                showFeedback(GestureFeedback.Brightness(next), autoHideMs = 600)
                            }
                            DragAxis.VERTICAL_VOLUME -> {
                                val next = (currentVolume - dragAmount.y / size.height).coerceIn(0f, 1f)
                                onVolumeChange(next)
                                showFeedback(GestureFeedback.Volume(next), autoHideMs = 600)
                            }
                            DragAxis.NONE -> Unit
                        }
                    },
                )
            },
    ) {
        feedback?.let { GestureFeedbackBubble(it, Modifier.align(Alignment.Center)) }
    }
}

private sealed interface GestureFeedback {
    data object SeekForward : GestureFeedback
    data object SeekBackward : GestureFeedback
    data class Seeking(val deltaMs: Long) : GestureFeedback
    data class Brightness(val value: Float) : GestureFeedback
    data class Volume(val value: Float) : GestureFeedback
    data object SpeedBoost : GestureFeedback
}

@Composable
private fun GestureFeedbackBubble(feedback: GestureFeedback, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.nexoraColors
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.scrimStrong)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (feedback) {
                is GestureFeedback.SeekForward -> {
                    Icon(Icons.Filled.Forward10, contentDescription = null, tint = colors.textPrimary)
                }
                is GestureFeedback.SeekBackward -> {
                    Icon(Icons.Filled.Replay10, contentDescription = null, tint = colors.textPrimary)
                }
                is GestureFeedback.Seeking -> {
                    val sign = if (feedback.deltaMs >= 0) "+" else ""
                    Text("$sign${feedback.deltaMs / 1000}s", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                }
                is GestureFeedback.Brightness -> {
                    Icon(Icons.Filled.Brightness6, contentDescription = null, tint = colors.textPrimary)
                    Text("${(feedback.value * 100).toInt()}%", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                }
                is GestureFeedback.Volume -> {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = colors.textPrimary)
                    Text("${(feedback.value * 100).toInt()}%", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                }
                is GestureFeedback.SpeedBoost -> {
                    Text("2x speed", color = colors.accent, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
