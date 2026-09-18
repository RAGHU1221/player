package com.nexora.player.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.player.domain.model.PlaylistSummary
import com.nexora.player.ui.components.EmptyState
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@Composable
fun PlaylistsScreen(onPlaylistClick: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: PlaylistsViewModel = viewModel(factory = GenericViewModelFactory { PlaylistsViewModel(app.playlistRepository) })
    val playlists by viewModel.playlists.collectAsState()
    val colors = MaterialTheme.nexoraColors
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = colors.accent) {
                Icon(Icons.Filled.Add, contentDescription = "Create Playlist", tint = Color.White)
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Playlists",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
            if (playlists.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No playlists yet",
                        subtitle = "Create a playlist to organize your videos",
                        icon = Icons.Filled.PlaylistPlay,
                        primaryActionLabel = "New Playlist",
                        onPrimaryAction = { showCreateDialog = true },
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 140.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        PlaylistTile(playlist, onClick = { onPlaylistClick(playlist.id) })
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            confirmButton = {
                TextButton(onClick = { viewModel.createPlaylist(name); showCreateDialog = false }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, placeholder = { Text("e.g. Workout") }, singleLine = true)
            },
        )
    }
}

@Composable
private fun PlaylistTile(playlist: PlaylistSummary, onClick: () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = colors.accent)
            Text(playlist.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1)
            Text("${playlist.itemCount} videos", style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
        }
    }
}
