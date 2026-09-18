package com.nexora.player.ui.playlists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.player.domain.model.Video
import com.nexora.player.ui.components.VideoCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@Composable
fun PlaylistDetailScreen(playlistId: Long, onBack: () -> Unit, onVideoClick: (Video) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: PlaylistDetailViewModel = viewModel(
        key = "playlist_detail_$playlistId",
        factory = GenericViewModelFactory { PlaylistDetailViewModel(playlistId, app.playlistRepository, app.database) },
    )
    val state by viewModel.state.collectAsState()
    val colors = MaterialTheme.nexoraColors
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary) }
            Text(
                state.playlist?.name ?: "Playlist",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Playlist", tint = colors.textSecondary)
            }
        }

        LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 140.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(state.videos, key = { _, video -> video.id }) { index, video ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                    VideoCard(
                        video = video,
                        modifier = Modifier.weight(1f),
                        onClick = { onVideoClick(video) },
                        onToggleFavorite = {},
                        onMoreClick = {},
                    )
                    Column {
                        IconButton(onClick = { viewModel.moveUp(video.id) }, enabled = index > 0) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up", tint = colors.textSecondary)
                        }
                        IconButton(onClick = { viewModel.moveDown(video.id) }, enabled = index < state.videos.lastIndex) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down", tint = colors.textSecondary)
                        }
                        IconButton(onClick = { viewModel.removeVideo(video.id) }) {
                            Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remove", tint = colors.accent)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this playlist?") },
            text = { Text("This won't delete the videos themselves.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePlaylist(onBack); showDeleteConfirm = false }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}
