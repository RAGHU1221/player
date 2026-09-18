package com.nexora.player.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(private val videoRepository: VideoRepository) : ViewModel() {
    val favorites: StateFlow<List<Video>> = videoRepository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFavorite(video: Video) {
        viewModelScope.launch { videoRepository.toggleFavorite(video.id, !video.isFavorite) }
    }
}
