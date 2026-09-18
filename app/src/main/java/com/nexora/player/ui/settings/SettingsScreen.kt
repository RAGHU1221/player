package com.nexora.player.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.player.data.datastore.RotationMode
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.theme.NexoraThemeVariant
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.ui.theme.toExtendedColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: SettingsViewModel = viewModel(
        factory = GenericViewModelFactory { SettingsViewModel(app.settingsDataStore, app.historyRepository, context.applicationContext) },
    )
    val settings by viewModel.settings.collectAsState()
    val colors = MaterialTheme.nexoraColors

    LazyColumn(contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 140.dp), modifier = Modifier.fillMaxSize()) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, modifier = Modifier.padding(bottom = 16.dp))
        }

        item {
            SettingsCategory("Appearance") {
                Text("Theme", color = colors.textSecondary, modifier = Modifier.padding(bottom = 8.dp))
                ScrollableChipRow {
                    NexoraThemeVariant.entries.forEach { variant ->
                        ThemeSwatch(variant = variant, selected = settings.themeVariant == variant, onClick = { viewModel.setTheme(variant) })
                    }
                }
            }
        }

        item {
            SettingsCategory("Playback") {
                SwitchRow("Autoplay next video", settings.autoplay, viewModel::setAutoplay)
                SwitchRow("Resume playback", settings.resumePlaybackEnabled, viewModel::setResumePlayback)
                SwitchRow("Background audio", settings.backgroundAudioEnabled, viewModel::setBackgroundAudio)
                SwitchRow("Remember last speed per video", settings.rememberLastSpeed, viewModel::setRememberLastSpeed)
                Text("Double-tap seek: ${settings.doubleTapSeekSeconds}s", color = colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
                ScrollableChipRow {
                    listOf(5, 10, 15, 30).forEach { seconds ->
                        ChoiceChip("${seconds}s", settings.doubleTapSeekSeconds == seconds) { viewModel.setDoubleTapSeek(seconds) }
                    }
                }
                Text("Default speed: ${settings.defaultPlaybackSpeed}x", color = colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
                ScrollableChipRow {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { speed ->
                        ChoiceChip("${speed}x", settings.defaultPlaybackSpeed == speed) { viewModel.setDefaultSpeed(speed) }
                    }
                }
            }
        }

        item {
            SettingsCategory("Video") {
                SwitchRow("Hardware acceleration", settings.hardwareAccelerationEnabled, viewModel::setHardwareAcceleration)
                SwitchRow("Allow software decoder fallback", settings.preferSoftwareFallback, viewModel::setSoftwareFallback)
                Text("Rotation", color = colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
                ScrollableChipRow {
                    RotationMode.entries.forEach { mode ->
                        ChoiceChip(mode.name.replace('_', ' '), settings.rotationMode == mode) { viewModel.setRotationMode(mode) }
                    }
                }
            }
        }

        item {
            SettingsCategory("Gestures") {
                SwitchRow("Enable gestures", settings.gesturesEnabled, viewModel::setGesturesEnabled)
                SwitchRow("Seek gesture", settings.seekGestureEnabled, viewModel::setSeekGesture)
                SwitchRow("Brightness gesture", settings.brightnessGestureEnabled, viewModel::setBrightnessGesture)
                SwitchRow("Volume gesture", settings.volumeGestureEnabled, viewModel::setVolumeGesture)
            }
        }

        item {
            SettingsCategory("Library & Storage") {
                SettingsActionRow("Clear watch history", "Removes all resume points") { viewModel.clearAllHistory() }
                SettingsActionRow("Clear thumbnail cache", "Frees up disk space; thumbnails regenerate automatically") { viewModel.clearThumbnailCache() }
            }
        }

        item {
            SettingsCategory("Privacy") {
                Text(
                    "Nexora Player never uploads your videos. All scanning, thumbnail generation and playback happen entirely on your device.",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            SettingsCategory("About") {
                Text("Nexora Player", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                Text("Version 1.0.0", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Keeps chip rows (themes, speeds, rotation modes) from overflowing on narrow phones (section 38). */
@Composable
private fun ScrollableChipRow(content: @Composable () -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) { content() }
}

@Composable
private fun SettingsCategory(title: String, content: @Composable () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.accent, modifier = Modifier.padding(bottom = 8.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.nexoraColors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = colors.textPrimary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = colors.accent, checkedThumbColor = androidx.compose.ui.graphics.Color.White),
        )
    }
}

@Composable
private fun SettingsActionRow(label: String, subtitle: String, onClick: () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    ) {
        Text(label, color = colors.textPrimary, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, color = colors.textTertiary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) colors.accent else colors.surfaceElevated,
            labelColor = if (selected) Color.White else colors.textSecondary,
        ),
        modifier = Modifier.padding(end = 8.dp),
    )
}

@Composable
private fun ThemeSwatch(variant: NexoraThemeVariant, selected: Boolean, onClick: () -> Unit) {
    val extended = remember(variant) { variant.toExtendedColors() }
    Column(
        modifier = Modifier
            .padding(end = 12.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(bottom = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(extended.accent)
                .then(if (selected) Modifier.border(2.dp, extended.textPrimary, CircleShape) else Modifier),
        )
        Text(variant.displayName, color = MaterialTheme.nexoraColors.textSecondary, style = MaterialTheme.typography.labelMedium)
    }
}
