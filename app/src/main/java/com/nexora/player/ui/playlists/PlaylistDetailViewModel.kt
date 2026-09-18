package com.nexora.player.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.database.entity.PlaylistEntity
import com.nexora.player.data.repository.PlaylistRepository
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistDetailState(val playlist: PlaylistEntity? = null, val videos: List<Video> = emptyList())

class PlaylistDetailViewModel(
    private val playlistId: Long,
    private val playlistRepository: PlaylistRepository,
    database: com.nexora.player.data.database.AppDatabase,
) : ViewModel() {

    private val playlistDao = database.playlistDao()

    val state: StateFlow<PlaylistDetailState> = combine(
        playlistDao.observeById(playlistId),
        playlistRepository.observeVideosInPlaylist(playlistId),
    ) { playlist, videos -> PlaylistDetailState(playlist, videos) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistDetailState())

    fun removeVideo(videoId: Long) {
        viewModelScope.launch { playlistRepository.removeVideo(playlistId, videoId) }
    }

    fun moveUp(videoId: Long) = reorder(videoId, -1)
    fun moveDown(videoId: Long) = reorder(videoId, 1)

    private fun reorder(videoId: Long, delta: Int) {
        val current = state.value.videos.map { it.id }.toMutableList()
        val index = current.indexOf(videoId)
        val targetIndex = index + delta
        if (index < 0 || targetIndex < 0 || targetIndex >= current.size) return
        current.removeAt(index)
        current.add(targetIndex, videoId)
        viewModelScope.launch { playlistRepository.reorder(playlistId, current) }
    }

    fun rename(newName: String) {
        val playlist = state.value.playlist ?: return
        viewModelScope.launch { playlistRepository.renamePlaylist(playlist, newName) }
    }

    fun deletePlaylist(onDeleted: () -> Unit) {
        val playlist = state.value.playlist ?: return
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlist)
            onDeleted()
        }
    }
}
