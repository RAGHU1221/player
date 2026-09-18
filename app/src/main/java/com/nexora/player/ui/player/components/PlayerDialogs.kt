package com.nexora.player.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexora.player.player.PlayerUiError
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.theme.NexoraRadius
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.FormatUtils

@Composable
fun ResumeDialog(resumePositionMs: Long, onResume: () -> Unit, onStartOver: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.nexoraColors
    Box(modifier = modifier.fillMaxSize().background(colors.scrimStrong), contentAlignment = Alignment.Center) {
        GlassCard(cornerRadius = NexoraRadius.CardLarge, containerColor = colors.surfaceElevated) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Resume playback?", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text(
                    "Continue from ${FormatUtils.formatDuration(resumePositionMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onStartOver) {
                        Text("Start Over", color = colors.textPrimary)
                    }
                    Button(
                        onClick = onResume,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White),
                    ) {
                        Text("Resume")
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerErrorOverlay(error: PlayerUiError, onRetry: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.nexoraColors
    Box(modifier = modifier.fillMaxSize().background(colors.gradientBottom), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = colors.accent, modifier = Modifier.padding(bottom = 16.dp))
            Text(
                error.message,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Row(modifier = Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Back", color = colors.textPrimary) }
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White)) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun SubtitleStyleSheet(
    scale: Float,
    delayMs: Long,
    backgroundEnabled: Boolean,
    onScaleChange: (Float) -> Unit,
    onDelayChange: (Long) -> Unit,
    onBackgroundToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.nexoraColors
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Subtitle Style", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)

            Text("Size", color = colors.textSecondary, modifier = Modifier.padding(top = 16.dp))
            Slider(
                value = scale,
                onValueChange = onScaleChange,
                valueRange = 0.6f..1.8f,
                colors = SliderDefaults.colors(thumbColor = colors.accent, activeTrackColor = colors.accent),
            )

            Text("Delay: ${delayMs / 1000.0}s", color = colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onDelayChange(delayMs - 500) }) { Text("-0.5s", color = colors.textPrimary) }
                OutlinedButton(onClick = { onDelayChange(delayMs + 500) }) { Text("+0.5s", color = colors.textPrimary) }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.material3.Switch(
                    checked = backgroundEnabled,
                    onCheckedChange = onBackgroundToggle,
                    colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = colors.accent),
                )
                Text("Background behind text", color = colors.textPrimary, modifier = Modifier.padding(start = 10.dp))
            }
        }
    }
}
