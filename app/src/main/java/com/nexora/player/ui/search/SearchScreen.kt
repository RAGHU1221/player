package com.nexora.player.ui.search

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun SearchScreen(onBack: () -> Unit, onVideoClick: (Video) -> Unit, onVideoMore: (Video) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: SearchViewModel = viewModel(
        factory = GenericViewModelFactory { SearchViewModel(app.videoRepository, app.playlistRepository) },
    )
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.nexoraColors

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Search by filename, folder or playlist", color = colors.textTertiary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = colors.textSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.glassBorder,
                    cursorColor = colors.accent,
                ),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
        }

        if (state.query.isBlank()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Try searching", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    state.recentQueries.forEach { suggestion ->
                        androidx.compose.material3.AssistChip(
                            onClick = { viewModel.onQueryChange(suggestion) },
                            label = { Text(suggestion) },
                        )
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                if (state.playlistResults.isNotEmpty()) {
                    item {
                        Text("Playlists", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary)
                    }
                    items(state.playlistResults, key = { "pl_${it.id}" }) { playlist ->
                        Text(
                            playlist.name,
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        )
                    }
                }
                if (state.videoResults.isNotEmpty()) {
                    item {
                        Text(
                            "Videos",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    items(state.videoResults, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            onClick = { onVideoClick(video) },
                            onToggleFavorite = {},
                            onMoreClick = { onVideoMore(video) },
                        )
                    }
                }
            }
        }
    }
}
