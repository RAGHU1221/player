package com.nexora.player.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.DeleteResult
import com.nexora.player.data.repository.PlaylistRepository
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.PlaylistSummary
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VideoDetailsState(val video: Video? = null, val playlists: List<PlaylistSummary> = emptyList())

class VideoDetailsViewModel(
    private val videoId: Long,
    private val videoRepository: VideoRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    val state: StateFlow<VideoDetailsState> = combine(
        videoRepository.observeById(videoId),
        playlistRepository.observePlaylists(),
    ) { video, playlists -> VideoDetailsState(video, playlists) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VideoDetailsState())

    private val _deleteResult = MutableStateFlow<DeleteResult?>(null)
    val deleteResult: StateFlow<DeleteResult?> = _deleteResult

    fun toggleFavorite() {
        val video = state.value.video ?: return
        viewModelScope.launch { videoRepository.toggleFavorite(video.id, !video.isFavorite) }
    }

    fun addToPlaylist(playlistId: Long) {
        viewModelScope.launch { playlistRepository.addVideo(playlistId, videoId) }
    }

    fun rename(newName: String) {
        val video = state.value.video ?: return
        if (newName.isBlank()) return
        viewModelScope.launch { videoRepository.renameVideo(video, newName.trim()) }
    }

    fun requestDelete() {
        val video = state.value.video ?: return
        viewModelScope.launch { _deleteResult.value = videoRepository.deleteVideo(video) }
    }

    fun confirmDeleteAfterUserConsent() {
        val video = state.value.video ?: return
        viewModelScope.launch {
            videoRepository.confirmDeleteAfterConsent(video)
            _deleteResult.value = DeleteResult.Success
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}
