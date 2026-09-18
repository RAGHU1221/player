package com.nexora.player.ui.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.nexora.player.domain.model.LibraryFilter
import com.nexora.player.domain.model.SortOption
import com.nexora.player.domain.model.Video
import com.nexora.player.ui.components.EmptyState
import com.nexora.player.ui.components.VideoCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp

@Composable
fun LibraryScreen(
    title: String = "All Videos",
    folderPath: String? = null,
    onBack: (() -> Unit)? = null,
    onVideoClick: (Video) -> Unit,
    onVideoMore: (Video) -> Unit,
) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: LibraryViewModel = viewModel(
        key = "library_$folderPath",
        factory = GenericViewModelFactory { LibraryViewModel(app.videoRepository, folderPath) },
    )
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.nexoraColors

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f).padding(start = if (onBack == null) 8.dp else 0.dp),
            )
            SortMenuButton(current = state.sortOption, onSelect = viewModel::setSortOption)
        }

        FilterChipRow(active = state.activeFilters, onToggle = viewModel::toggleFilter)

        if (state.videos.isEmpty() && !state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(title = "No videos match", subtitle = "Try clearing filters or scanning your library again")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 168.dp),
                contentPadding = PaddingValues(12.dp, 4.dp, 12.dp, 140.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.videos, key = { it.id }) { video ->
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

@Composable
private fun SortMenuButton(current: SortOption, onSelect: (SortOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val colors = MaterialTheme.nexoraColors
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.Sort, contentDescription = "Sort", tint = colors.textPrimary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun FilterChipRow(active: Set<LibraryFilter>, onToggle: (LibraryFilter) -> Unit) {
    val colors = MaterialTheme.nexoraColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryFilter.entries.filter { it != LibraryFilter.ALL }.forEach { filter ->
            FilterChip(
                selected = filter in active,
                onClick = { onToggle(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.accent,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                    containerColor = colors.surface,
                    labelColor = colors.textSecondary,
                ),
            )
        }
    }
}
