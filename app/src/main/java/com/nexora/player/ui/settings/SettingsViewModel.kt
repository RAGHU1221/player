package com.nexora.player.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.player.data.datastore.AppSettings
import com.nexora.player.data.datastore.RotationMode
import com.nexora.player.data.datastore.SettingsDataStore
import com.nexora.player.data.media.ThumbnailProvider
import com.nexora.player.data.repository.HistoryRepository
import com.nexora.player.ui.theme.NexoraThemeVariant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val historyRepository: HistoryRepository,
    private val appContext: android.content.Context,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsDataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTheme(variant: NexoraThemeVariant) = launch { settingsDataStore.setTheme(variant) }
    fun setAutoplay(value: Boolean) = launch { settingsDataStore.setAutoplay(value) }
    fun setResumePlayback(value: Boolean) = launch { settingsDataStore.setResumePlaybackEnabled(value) }
    fun setBackgroundAudio(value: Boolean) = launch { settingsDataStore.setBackgroundAudioEnabled(value) }
    fun setDoubleTapSeek(seconds: Int) = launch { settingsDataStore.setDoubleTapSeekSeconds(seconds) }
    fun setDefaultSpeed(speed: Float) = launch { settingsDataStore.setDefaultPlaybackSpeed(speed) }
    fun setRememberLastSpeed(value: Boolean) = launch { settingsDataStore.setRememberLastSpeed(value) }
    fun setGesturesEnabled(value: Boolean) = launch { settingsDataStore.setGesturesEnabled(value) }
    fun setSeekGesture(value: Boolean) = launch { settingsDataStore.setSeekGestureEnabled(value) }
    fun setBrightnessGesture(value: Boolean) = launch { settingsDataStore.setBrightnessGestureEnabled(value) }
    fun setVolumeGesture(value: Boolean) = launch { settingsDataStore.setVolumeGestureEnabled(value) }
    fun setHardwareAcceleration(value: Boolean) = launch { settingsDataStore.setHardwareAccelerationEnabled(value) }
    fun setSoftwareFallback(value: Boolean) = launch { settingsDataStore.setPreferSoftwareFallback(value) }
    fun setRotationMode(mode: RotationMode) = launch { settingsDataStore.setRotationMode(mode) }

    fun clearAllHistory() = launch { historyRepository.clearAllHistory() }
    fun clearThumbnailCache() = launch { ThumbnailProvider.clearCache(appContext) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
