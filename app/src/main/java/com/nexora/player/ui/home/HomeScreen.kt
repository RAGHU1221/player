package com.nexora.player.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.nexora.player.domain.model.PlaylistSummary
import com.nexora.player.domain.model.Video
import com.nexora.player.ui.components.EmptyState
import com.nexora.player.ui.components.GlassCard
import com.nexora.player.ui.components.NexoraLogo
import com.nexora.player.ui.components.SectionHeader
import com.nexora.player.ui.components.VideoCard
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.PermissionUtils
import com.nexora.player.util.nexoraApp

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onVideoClick: (Video) -> Unit,
    onVideoMore: (Video) -> Unit,
    onSeeAllVideos: () -> Unit,
    onSeeAllFavorites: () -> Unit,
    onSeeAllPlaylists: () -> Unit,
    onFoldersClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChooseFolder: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val viewModel: HomeViewModel = viewModel(
        factory = GenericViewModelFactory { HomeViewModel(app.videoRepository, app.playlistRepository) },
    )
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.nexoraColors

    val permissionState = rememberPermissionState(PermissionUtils.mediaPermission)

    Box(modifier = Modifier.fillMaxSize()) {
        if (!permissionState.status.isGranted) {
            PermissionRequestContent(onGrant = { permissionState.launchPermissionRequest() })
        } else if (state.isEmpty) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeTopBar(onSearchClick, onSettingsClick)
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "No videos found",
                        subtitle = "Scan your device or choose a folder to build your library",
                        primaryActionLabel = "Scan Storage",
                        onPrimaryAction = { viewModel.scan() },
                        secondaryActionLabel = "Choose Folder",
                        onSecondaryAction = onChooseFolder,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp),
            ) {
                item { HomeTopBar(onSearchClick, onSettingsClick) }

                if (state.isScanning) {
                    item { ScanningBanner() }
                }

                if (state.continueWatching.isNotEmpty()) {
                    item {
                        SectionHeader("Continue Watching", onActionClick = onSeeAllVideos)
                        VideoRow(state.continueWatching, onVideoClick, onVideoMore, viewModel)
                    }
                }

                item {
                    SectionHeader("Recently Added", onActionClick = onSeeAllVideos)
                    VideoRow(state.recentlyAdded, onVideoClick, onVideoMore, viewModel)
                }

                item {
                    QuickStatsRow(
                        totalVideos = state.totalVideoCount,
                        folderCount = state.folderCount,
                        onAllVideosClick = onSeeAllVideos,
                        onFoldersClick = onFoldersClick,
                        onPlaylistsClick = onSeeAllPlaylists,
                    )
                }

                if (state.favorites.isNotEmpty()) {
                    item {
                        SectionHeader("Favorites", onActionClick = onSeeAllFavorites)
                        VideoRow(state.favorites, onVideoClick, onVideoMore, viewModel)
                    }
                }

                if (state.playlists.isNotEmpty()) {
                    item {
                        SectionHeader("Playlists", onActionClick = onSeeAllPlaylists)
                        PlaylistRow(state.playlists)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(onSearchClick: () -> Unit, onSettingsClick: () -> Unit) {
    val colors = MaterialTheme.nexoraColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NexoraLogo()
        Row {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = colors.textPrimary)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = colors.textPrimary)
            }
        }
    }
}

@Composable
private fun ScanningBanner() {
    val colors = MaterialTheme.nexoraColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.accent)
        Text(
            "Scanning your library…",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VideoRow(
    videos: List<Video>,
    onVideoClick: (Video) -> Unit,
    onVideoMore: (Video) -> Unit,
    viewModel: HomeViewModel,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(videos, key = { it.id }) { video ->
            VideoCard(
                video = video,
                modifier = Modifier.width(220.dp),
                onClick = { onVideoClick(video) },
                onToggleFavorite = { viewModel.toggleFavorite(video) },
                onMoreClick = { onVideoMore(video) },
            )
        }
    }
}

@Composable
private fun PlaylistRow(playlists: List<PlaylistSummary>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(playlists, key = { it.id }) { playlist ->
            GlassCard(modifier = Modifier.size(140.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = MaterialTheme.nexoraColors.accent)
                    Text(
                        playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.nexoraColors.textPrimary,
                        maxLines = 1,
                    )
                    Text(
                        "${playlist.itemCount} videos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.nexoraColors.textTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    totalVideos: Int,
    folderCount: Int,
    onAllVideosClick: () -> Unit,
    onFoldersClick: () -> Unit,
    onPlaylistsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatTile(icon = Icons.Filled.Search, label = "All Videos", value = "$totalVideos", onClick = onAllVideosClick, modifier = Modifier.weight(1f))
        StatTile(icon = Icons.Filled.FolderOpen, label = "Folders", value = "$folderCount", onClick = onFoldersClick, modifier = Modifier.weight(1f))
        StatTile(icon = Icons.Filled.PlaylistPlay, label = "Playlists", value = "", onClick = onPlaylistsClick, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.nexoraColors
    GlassCard(modifier = modifier.combinedClickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
            Text(value.ifBlank { label }, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            if (value.isNotBlank()) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(onGrant: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = "Allow access to your videos",
            subtitle = "Nexora Player needs access to your videos to build your library. No files ever leave your device.",
            primaryActionLabel = "Grant Access",
            onPrimaryAction = onGrant,
        )
    }
}
