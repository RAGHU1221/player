package com.nexora.player.ui.history

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.nexora.player.ui.components.EmptyState
import com.nexora.player.ui.components.VideoCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@Composable
fun HistoryScreen(onBack: () -> Unit, onVideoClick: (Video) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: HistoryViewModel = viewModel(
        factory = GenericViewModelFactory { HistoryViewModel(app.videoRepository, app.historyRepository) },
    )
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.nexoraColors

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary) }
            Text("Watch History", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.clearAll() }) {
                Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history", tint = colors.textSecondary)
            }
        }

        if (state.continueWatching.isEmpty() && state.completed.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(title = "No watch history yet", subtitle = "Videos you watch will show up here", icon = Icons.Filled.History)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 140.dp), modifier = Modifier.fillMaxSize()) {
                if (state.continueWatching.isNotEmpty()) {
                    item {
                        Text("Continue Watching", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(state.continueWatching, key = { "cw_${it.id}" }) { video ->
                        HistoryRow(video, onVideoClick, onRemove = { viewModel.removeFromHistory(video.id) })
                    }
                }
                if (state.completed.isNotEmpty()) {
                    item {
                        Text("Completed", style = MaterialTheme.typography.titleMedium, color = colors.textSecondary, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    items(state.completed, key = { "done_${it.id}" }) { video ->
                        HistoryRow(video, onVideoClick, onRemove = { viewModel.removeFromHistory(video.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(video: Video, onVideoClick: (Video) -> Unit, onRemove: () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        VideoCard(
            video = video,
            modifier = Modifier.weight(1f),
            onClick = { onVideoClick(video) },
            onToggleFavorite = {},
            onMoreClick = onRemove,
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remove from history", tint = colors.textSecondary)
        }
    }
}
