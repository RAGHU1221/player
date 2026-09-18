package com.nexora.player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.PlaylistRepository
import com.nexora.player.domain.model.PlaylistSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistsViewModel(private val playlistRepository: PlaylistRepository) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<PlaylistSummary>> = playlistRepository.observePlaylists()
        .flatMapLatest { summaries ->
            if (summaries.isEmpty()) {
                flowOf(emptyList())
            } else {
                val perPlaylistFlows = summaries.map { summary ->
                    playlistRepository.observeItemCount(summary.id).map { count -> summary.copy(itemCount = count) }
                }
                combine(perPlaylistFlows) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { playlistRepository.createPlaylist(name.trim()) }
    }
}
