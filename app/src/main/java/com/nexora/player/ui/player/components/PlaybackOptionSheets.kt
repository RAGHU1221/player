package com.nexora.player.ui.player.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SubtitlesOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nexora.player.domain.model.AspectRatioMode
import com.nexora.player.domain.model.AudioTrackInfo
import com.nexora.player.domain.model.PLAYBACK_SPEEDS
import com.nexora.player.domain.model.SubtitleTrackInfo
import com.nexora.player.ui.theme.nexoraColors
import androidx.compose.foundation.clickable

@Composable
fun SpeedSheet(currentSpeed: Float, onSelect: (Float) -> Unit, onDismiss: () -> Unit) {
    OptionSheet(title = "Playback Speed", onDismiss = onDismiss) {
        PLAYBACK_SPEEDS.forEach { speed ->
            OptionRow(
                label = if (speed == 1.0f) "Normal (1.0x)" else "${speed}x",
                selected = speed == currentSpeed,
                onClick = { onSelect(speed); onDismiss() },
            )
        }
    }
}

@Composable
fun AspectRatioSheet(current: AspectRatioMode, onSelect: (AspectRatioMode) -> Unit, onDismiss: () -> Unit) {
    OptionSheet(title = "Aspect Ratio", onDismiss = onDismiss) {
        AspectRatioMode.entries.forEach { mode ->
            OptionRow(label = mode.label, selected = mode == current, onClick = { onSelect(mode); onDismiss() })
        }
    }
}

@Composable
fun AudioTrackSheet(tracks: List<AudioTrackInfo>, onSelect: (AudioTrackInfo) -> Unit, onDismiss: () -> Unit) {
    OptionSheet(title = "Audio Track", onDismiss = onDismiss) {
        if (tracks.isEmpty()) {
            Text(
                "Only one audio track is available for this video.",
                color = MaterialTheme.nexoraColors.textSecondary,
                modifier = Modifier.padding(16.dp),
            )
        }
        tracks.forEach { track ->
            OptionRow(label = track.displayLabel, selected = track.isSelected, onClick = { onSelect(track); onDismiss() })
        }
    }
}

@Composable
fun SubtitleTrackSheet(
    tracks: List<SubtitleTrackInfo>,
    subtitlesEnabled: Boolean,
    onSelect: (SubtitleTrackInfo?) -> Unit,
    onOpenStyleSettings: () -> Unit,
    onLoadExternal: () -> Unit,
    onDismiss: () -> Unit,
) {
    OptionSheet(title = "Subtitles", onDismiss = onDismiss) {
        OptionRow(
            label = "Off",
            selected = !subtitlesEnabled,
            icon = Icons.Filled.SubtitlesOff,
            onClick = { onSelect(null); onDismiss() },
        )
        tracks.forEach { track ->
            OptionRow(
                label = "${track.language} • ${track.format}",
                selected = subtitlesEnabled && track.isSelected,
                onClick = { onSelect(track); onDismiss() },
            )
        }
        HorizontalDivider(color = MaterialTheme.nexoraColors.divider, modifier = Modifier.padding(vertical = 6.dp))
        OptionRow(label = "Load subtitle file (.srt/.vtt/.ass)…", selected = false, onClick = { onLoadExternal(); onDismiss() })
        OptionRow(label = "Subtitle style & delay…", selected = false, onClick = { onOpenStyleSettings(); onDismiss() })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionSheet(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        contentColor = colors.textPrimary,
    ) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.nexoraColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = colors.accent, unselectedColor = colors.textTertiary),
        )
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.padding(end = 8.dp))
        }
        Text(label, color = colors.textPrimary, style = MaterialTheme.typography.bodyLarge)
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = colors.accent, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
