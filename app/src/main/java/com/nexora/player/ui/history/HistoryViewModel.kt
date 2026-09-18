package com.nexora.player.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.repository.HistoryRepository
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(val continueWatching: List<Video> = emptyList(), val completed: List<Video> = emptyList())

class HistoryViewModel(
    private val videoRepository: VideoRepository,
    private val historyRepository: HistoryRepository,
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = videoRepository.observeLibrary().map { videos ->
        val withHistory = videos.filter { it.watchProgress != null }
            .sortedByDescending { it.watchProgress?.lastPlayedAtEpochMs }
        HistoryUiState(
            continueWatching = withHistory.filter { it.isStarted },
            completed = withHistory.filter { it.isCompleted },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun removeFromHistory(videoId: Long) {
        viewModelScope.launch { historyRepository.clearProgress(videoId) }
    }

    fun clearAll() {
        viewModelScope.launch { historyRepository.clearAllHistory() }
    }
}
