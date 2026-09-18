package com.nexora.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.PlaylistRepository
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.PlaylistSummary
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val continueWatching: List<Video> = emptyList(),
    val recentlyAdded: List<Video> = emptyList(),
    val favorites: List<Video> = emptyList(),
    val folderCount: Int = 0,
    val playlists: List<PlaylistSummary> = emptyList(),
    val totalVideoCount: Int = 0,
    val isScanning: Boolean = false,
    val isEmpty: Boolean = false,
)

class HomeViewModel(
    private val videoRepository: VideoRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val isScanning = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        videoRepository.observeContinueWatching(limit = 10),
        videoRepository.observeLibrary(),
        videoRepository.observeFavorites(),
        videoRepository.observeFolderPaths(),
        playlistRepository.observePlaylists(),
        isScanning,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val continueWatching = flows[0] as List<Video>
        @Suppress("UNCHECKED_CAST")
        val library = flows[1] as List<Video>
        @Suppress("UNCHECKED_CAST")
        val favorites = flows[2] as List<Video>
        @Suppress("UNCHECKED_CAST")
        val folders = flows[3] as List<String>
        @Suppress("UNCHECKED_CAST")
        val playlists = flows[4] as List<PlaylistSummary>
        val scanning = flows[5] as Boolean

        HomeUiState(
            continueWatching = continueWatching,
            recentlyAdded = library.sortedByDescending { it.dateAddedEpochMs }.take(10),
            favorites = favorites.take(10),
            folderCount = folders.size,
            playlists = playlists.take(6),
            totalVideoCount = library.size,
            isScanning = scanning,
            isEmpty = library.isEmpty() && !scanning,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    init {
        scan()
    }

    fun scan() {
        viewModelScope.launch {
            isScanning.value = true
            val found = videoRepository.scanLibrary()
            if (found > 0) {
                videoRepository.probeUnprocessedVideos()
            }
            isScanning.value = false
        }
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch { videoRepository.toggleFavorite(video.id, !video.isFavorite) }
    }
}
