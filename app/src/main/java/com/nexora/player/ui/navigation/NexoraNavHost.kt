package com.nexora.player.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexora.player.player.PlayerUiState
import com.nexora.player.ui.components.PremiumBottomNav
import com.nexora.player.ui.details.VideoDetailsScreen
import com.nexora.player.ui.favorites.FavoritesScreen
import com.nexora.player.ui.folders.FoldersScreen
import com.nexora.player.ui.history.HistoryScreen
import com.nexora.player.ui.home.HomeScreen
import com.nexora.player.ui.library.LibraryScreen
import com.nexora.player.ui.miniplayer.MiniPlayerBar
import com.nexora.player.ui.player.PlayerScreen
import com.nexora.player.ui.playlists.PlaylistDetailScreen
import com.nexora.player.ui.playlists.PlaylistsScreen
import com.nexora.player.ui.search.SearchScreen
import com.nexora.player.ui.settings.SettingsScreen
import com.nexora.player.util.nexoraApp

@OptIn(UnstableApi::class)
@Composable
fun NexoraNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.nexoraApp()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showChrome = currentRoute in NexoraDestination.TOP_LEVEL_ROUTES

    // The last video the user opened, so the mini-player can re-open the exact
    // same PlayerScreen instance even after its back-stack entry was popped.
    var lastPlayedVideoId by remember { mutableStateOf<Long?>(null) }

    fun openPlayer(videoId: Long) {
        lastPlayedVideoId = videoId
        navController.navigate(NexoraDestination.Player.createRoute(videoId))
    }

    val playerController = app.playerController
    val playback: PlayerUiState? = playerController?.state?.collectAsState()?.value

    val showMiniPlayer = showChrome &&
        playback != null &&
        playback.mediaTitle.isNotBlank() &&
        currentRoute != NexoraDestination.Player.route &&
        lastPlayedVideoId != null

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showChrome) {
                Column {
                    if (showMiniPlayer && playback != null && playerController != null) {
                        MiniPlayerBar(
                            videoUri = playerController.player.currentMediaItem?.localConfiguration?.uri?.toString().orEmpty(),
                            title = playback.mediaTitle,
                            playback = playback,
                            onPlayPause = { playerController.playPause() },
                            onClose = { playerController.stopAndClear() },
                            onExpand = { lastPlayedVideoId?.let { openPlayer(it) } },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    PremiumBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NexoraDestination.Home.route,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(NexoraDestination.Home.route) {
                HomeScreen(
                    onVideoClick = { video -> openPlayer(video.id) },
                    onVideoMore = { video -> navController.navigate(NexoraDestination.VideoDetails.createRoute(video.id)) },
                    onSeeAllVideos = { navController.navigate(NexoraDestination.Library.route) },
                    onSeeAllFavorites = { navController.navigate(NexoraDestination.Favorites.route) },
                    onSeeAllPlaylists = { navController.navigate(NexoraDestination.Playlists.route) },
                    onFoldersClick = { navController.navigate(NexoraDestination.Folders.route) },
                    onSearchClick = { navController.navigate(NexoraDestination.Search.route) },
                    onSettingsClick = { navController.navigate(NexoraDestination.Settings.route) },
                    onChooseFolder = { navController.navigate(NexoraDestination.Folders.route) },
                )
            }
            composable(NexoraDestination.Library.route) {
                LibraryScreen(
                    onVideoClick = { video -> openPlayer(video.id) },
                    onVideoMore = { video -> navController.navigate(NexoraDestination.VideoDetails.createRoute(video.id)) },
                )
            }
            composable(NexoraDestination.Folders.route) {
                FoldersScreen(onFolderClick = { path -> navController.navigate(NexoraDestination.FolderContents.createRoute(path)) })
            }
            composable(
                route = NexoraDestination.FolderContents.route,
                arguments = listOf(navArgument(NexoraDestination.FolderContents.ARG_FOLDER_PATH) { type = androidx.navigation.NavType.StringType }),
            ) { entry ->
                val encoded = entry.arguments?.getString(NexoraDestination.FolderContents.ARG_FOLDER_PATH).orEmpty()
                val path = java.net.URLDecoder.decode(encoded, "UTF-8")
                LibraryScreen(
                    title = path.substringAfterLast('/'),
                    folderPath = path,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video -> openPlayer(video.id) },
                    onVideoMore = { video -> navController.navigate(NexoraDestination.VideoDetails.createRoute(video.id)) },
                )
            }
            composable(NexoraDestination.Playlists.route) {
                PlaylistsScreen(onPlaylistClick = { id -> navController.navigate(NexoraDestination.PlaylistDetail.createRoute(id)) })
            }
            composable(
                route = NexoraDestination.PlaylistDetail.route,
                arguments = listOf(navArgument(NexoraDestination.PlaylistDetail.ARG_PLAYLIST_ID) { type = androidx.navigation.NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong(NexoraDestination.PlaylistDetail.ARG_PLAYLIST_ID) ?: 0L
                PlaylistDetailScreen(
                    playlistId = id,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video -> openPlayer(video.id) },
                )
            }
            composable(NexoraDestination.Favorites.route) {
                FavoritesScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video -> openPlayer(video.id) },
                    onVideoMore = { video -> navController.navigate(NexoraDestination.VideoDetails.createRoute(video.id)) },
                )
            }
            composable(NexoraDestination.History.route) {
                HistoryScreen(onBack = { navController.popBackStack() }, onVideoClick = { video -> openPlayer(video.id) })
            }
            composable(NexoraDestination.Search.route) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onVideoClick = { video -> openPlayer(video.id) },
                    onVideoMore = { video -> navController.navigate(NexoraDestination.VideoDetails.createRoute(video.id)) },
                )
            }
            composable(NexoraDestination.Settings.route) { SettingsScreen() }
            composable(
                route = NexoraDestination.VideoDetails.route,
                arguments = listOf(navArgument(NexoraDestination.VideoDetails.ARG_VIDEO_ID) { type = androidx.navigation.NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong(NexoraDestination.VideoDetails.ARG_VIDEO_ID) ?: 0L
                VideoDetailsScreen(videoId = id, onBack = { navController.popBackStack() }, onPlay = { videoId -> openPlayer(videoId) })
            }
            composable(
                route = NexoraDestination.Player.route,
                arguments = listOf(navArgument(NexoraDestination.Player.ARG_VIDEO_ID) { type = androidx.navigation.NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong(NexoraDestination.Player.ARG_VIDEO_ID) ?: 0L
                PlayerScreen(
                    videoId = id,
                    onBack = { navController.popBackStack() },
                    onOpenDetails = { videoId -> navController.navigate(NexoraDestination.VideoDetails.createRoute(videoId)) },
                )
            }
        }
    }
}
