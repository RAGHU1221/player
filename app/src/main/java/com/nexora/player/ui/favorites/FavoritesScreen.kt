package com.nexora.player.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
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
fun FavoritesScreen(onBack: () -> Unit, onVideoClick: (Video) -> Unit, onVideoMore: (Video) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: FavoritesViewModel = viewModel(factory = GenericViewModelFactory { FavoritesViewModel(app.videoRepository) })
    val videos by viewModel.favorites.collectAsState()
    val colors = MaterialTheme.nexoraColors

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary) }
            Text("Favorites", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        }

        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = "No favorites yet",
                    subtitle = "Tap the heart icon on any video to add it here",
                    icon = Icons.Outlined.FavoriteBorder,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 168.dp),
                contentPadding = PaddingValues(12.dp, 4.dp, 12.dp, 140.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(videos, key = { it.id }) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) },
                        onToggleFavorite = { viewModel.toggleFavorite(video) },
                        onMoreClick = { onVideoMore(video) },
                    )
                }
            }
        }
    }
}
