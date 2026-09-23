package com.nexora.player.ui.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexora.player.player.PlayerUiState
import com.nexora.player.ui.theme.nexoraColors

@Composable
fun ControlsOverlay(
    visible: Boolean,
    title: String,
    videoUri: String,
    playback: PlayerUiState,
    is4kBadgeVisible: Boolean,
    codecLabel: String,
    onBack: () -> Unit,
    onMore: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekPreview: (Long) -> Unit,
    onSeekCommit: (Long) -> Unit,
    onLock: () -> Unit,
    onSubtitleClick: () -> Unit,
    onAudioClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onAspectRatioClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPipClick: () -> Unit,
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.nexoraColors
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to colors.scrimStrong,
                        0.18f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1f to colors.scrimStrong,
                    ),
                ),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (is4kBadgeVisible) {
                        Text(
                            "4K UHD • $codecLabel",
                            color = colors.accent,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                IconButton(onClick = onPipClick) {
                    Icon(Icons.Filled.PictureInPictureAlt, contentDescription = "Picture in Picture", tint = Color.White)
                }
                IconButton(onClick = onMore) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }

            // Center transport
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPrevious, enabled = hasPrevious, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Previous video",
                        tint = Color.White.copy(alpha = if (hasPrevious) 1f else 0.35f),
                        modifier = Modifier.size(30.dp),
                    )
                }
                IconButton(onClick = onSeekBack, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Filled.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                CenterPlayButton(playback = playback, onClick = onPlayPause)
                IconButton(onClick = onSeekForward, modifier = Modifier.size(52.dp)) {
                    Icon(Icons.Filled.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = onNext, enabled = hasNext, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Next video",
                        tint = Color.White.copy(alpha = if (hasNext) 1f else 0.35f),
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            // Lock button (top-end, below Pip/More so it's reachable one-handed)
            IconButton(
                onClick = onLock,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 12.dp),
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = "Lock controls", tint = Color.White.copy(alpha = 0.7f))
            }

            // Bottom bar: action row + seek bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                PremiumSeekBar(
                    positionMs = playback.positionMs,
                    durationMs = playback.durationMs,
                    bufferedMs = playback.bufferedPositionMs,
                    videoUri = videoUri,
                    onSeekPreview = onSeekPreview,
                    onSeekCommit = onSeekCommit,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    BottomIconAction(Icons.Filled.Subtitles, "Subtitles", onSubtitleClick)
                    BottomIconAction(Icons.Filled.Audiotrack, "Audio", onAudioClick)
                    BottomIconAction(Icons.Filled.Speed, "Speed (${playback.playbackSpeed}x)", onSpeedClick)
                    BottomIconAction(Icons.Filled.AspectRatio, "Aspect Ratio", onAspectRatioClick)
                    BottomIconAction(Icons.Filled.Settings, "Settings", onSettingsClick)
                }
            }

            if (playback.isBuffering) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center).size(48.dp),
                    color = colors.accent,
                )
            }
        }
    }
}

@Composable
private fun CenterPlayButton(playback: PlayerUiState, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(72.dp)) {
        Icon(
            imageVector = if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (playback.isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(56.dp),
        )
    }
}

@Composable
private fun BottomIconAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = label, tint = Color.White)
    }
}

@Composable
fun LockedOverlay(onUnlock: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.nexoraColors
    Box(modifier = modifier.fillMaxSize()) {
        IconButton(onClick = onUnlock, modifier = Modifier.align(Alignment.CenterEnd).padding(20.dp)) {
            Icon(Icons.Filled.Lock, contentDescription = "Unlock controls", tint = Color.White.copy(alpha = 0.8f))
        }
    }
}
