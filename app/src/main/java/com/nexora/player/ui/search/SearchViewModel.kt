package com.nexora.player.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.PlaylistRepository
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.PlaylistSummary
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val videoResults: List<Video> = emptyList(),
    val playlistResults: List<PlaylistSummary> = emptyList(),
    val recentQueries: List<String> = listOf("4K", "Tamil", "Movies"),
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val videoRepository: VideoRepository,
    playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val videoResults = query.debounce(200).flatMapLatest { q ->
        if (q.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList()) else videoRepository.search(q)
    }

    private val allPlaylists = playlistRepository.observePlaylists()

    val uiState: StateFlow<SearchUiState> = combine(query, videoResults, allPlaylists) { q, videos, playlists ->
        SearchUiState(
            query = q,
            videoResults = videos,
            playlistResults = if (q.isBlank()) emptyList() else playlists.filter { it.name.contains(q, ignoreCase = true) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }
}
