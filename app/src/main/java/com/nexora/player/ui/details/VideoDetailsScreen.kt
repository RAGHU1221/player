package com.nexora.player.ui.details

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.player.data.repository.DeleteResult
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.FormatUtils
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailsScreen(videoId: Long, onBack: () -> Unit, onPlay: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: VideoDetailsViewModel = viewModel(
        key = "details_$videoId",
        factory = GenericViewModelFactory { VideoDetailsViewModel(videoId, app.videoRepository, app.playlistRepository) },
    )
    val state by viewModel.state.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()
    val colors = MaterialTheme.nexoraColors
    val video = state.video

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showPlaylistSheet by remember { mutableStateOf(false) }

    val intentSenderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.confirmDeleteAfterUserConsent()
        } else {
            viewModel.clearDeleteResult()
        }
    }

    LaunchedEffect(deleteResult) {
        when (val result = deleteResult) {
            is DeleteResult.RequiresUserConsent -> {
                intentSenderLauncher.launch(IntentSenderRequest.Builder(result.intentSender).build())
            }
            is DeleteResult.Success -> onBack()
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary) }
            Text("Video Details", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        }

        if (video == null) return@Column

        LazyColumn(contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 140.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Text(video.filename, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text(video.folderPath, style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary, modifier = Modifier.padding(bottom = 16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 20.dp)) {
                    Button(onClick = { onPlay(videoId) }, colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.White)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Play")
                    }
                    OutlinedButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (video.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = null,
                            tint = if (video.isFavorite) colors.accent else colors.textPrimary,
                        )
                    }
                    OutlinedButton(onClick = { showPlaylistSheet = true }) {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = "Add to Playlist", tint = colors.textPrimary)
                    }
                    OutlinedButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "video/*"
                            putExtra(Intent.EXTRA_STREAM, Uri.parse(video.uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share video"))
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = colors.textPrimary)
                    }
                    OutlinedButton(onClick = { showRenameDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = colors.textPrimary)
                    }
                    OutlinedButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = colors.accent)
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Resolution", "${video.width} × ${video.height} (${video.resolution.label})")
                        DetailRow("FPS", FormatUtils.formatFrameRate(video.frameRate))
                        DetailRow("Codec", video.videoCodec.ifBlank { "Detecting…" })
                        DetailRow("Bitrate", FormatUtils.formatBitrate(video.bitrateBps))
                        DetailRow("Audio", listOfNotNull(video.audioCodec, "${video.audioChannels}ch").joinToString(" • "))
                        DetailRow("Subtitle tracks", "${video.subtitleTrackCount}")
                        DetailRow("HDR", video.hdrType.label)
                        DetailRow("Duration", FormatUtils.formatDuration(video.durationMs))
                        DetailRow("File size", FormatUtils.formatFileSize(video.sizeBytes))
                        DetailRow("Date modified", FormatUtils.formatRelativeDate(video.dateModifiedEpochMs), showDivider = false)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this video?") },
            text = { Text("This will permanently delete the file from your device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; viewModel.requestDelete() }) { Text("Delete", color = colors.accent) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }

    if (showRenameDialog && video != null) {
        var name by remember { mutableStateOf(video.filename) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename video") },
            text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) },
            confirmButton = { TextButton(onClick = { viewModel.rename(name); showRenameDialog = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } },
        )
    }

    if (showPlaylistSheet) {
        ModalBottomSheet(onDismissRequest = { showPlaylistSheet = false }, containerColor = colors.surfaceElevated) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text("Add to Playlist", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary, modifier = Modifier.padding(20.dp))
                state.playlists.forEach { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                    ) {
                        Text(playlist.name, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.addToPlaylist(playlist.id); showPlaylistSheet = false }) {
                            Text("Add", color = colors.accent)
                        }
                    }
                }
                if (state.playlists.isEmpty()) {
                    Text("No playlists yet — create one from the Playlists tab.", color = colors.textSecondary, modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, showDivider: Boolean = true) {
    val colors = MaterialTheme.nexoraColors
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = colors.textTertiary, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = colors.textPrimary, style = MaterialTheme.typography.bodyMedium)
        }
        if (showDivider) {
            androidx.compose.material3.HorizontalDivider(color = colors.divider)
        }
    }
}
