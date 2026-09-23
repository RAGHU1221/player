package com.nexora.player.ui.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.nexora.player.data.database.entity.PlaybackSettingsEntity
import com.nexora.player.data.datastore.AppSettings
import com.nexora.player.data.datastore.SettingsDataStore
import com.nexora.player.data.repository.HistoryRepository
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.domain.model.AspectRatioMode
import com.nexora.player.domain.model.AudioTrackInfo
import com.nexora.player.domain.model.SubtitleTrackInfo
import com.nexora.player.domain.model.Video
import com.nexora.player.player.PlayerController
import com.nexora.player.player.PlayerUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerScreenState(
    val video: Video? = null,
    val playback: PlayerUiState = PlayerUiState(),
    val pendingResumePositionMs: Long? = null,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isLocked: Boolean = false,
    val appSettings: AppSettings = AppSettings(),
    val zoomScale: Float = 1f,
    val isReady: Boolean = false,
    /** Id of the previous/next video in the same folder (by filename order), or
     * null at either end of the folder — drives the Previous/Next transport buttons. */
    val previousVideoId: Long? = null,
    val nextVideoId: Long? = null,
)

/**
 * Owns everything the cinematic player screen needs beyond raw playback (which
 * lives in the shared [PlayerController]): resume-or-start-over gating, per-video
 * aspect ratio/speed memory, lock state, pinch-zoom, and external subtitle
 * loading. One instance per [videoId] — created fresh by the nav host whenever
 * the user opens a different video (see NexoraNavHost).
 */
@UnstableApi
class PlayerViewModel(
    private val videoId: Long,
    private val videoRepository: VideoRepository,
    private val historyRepository: HistoryRepository,
    private val settingsDataStore: SettingsDataStore,
    val controller: PlayerController,
) : ViewModel() {

    private val aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
    private val isLocked = MutableStateFlow(false)
    private val pendingResume = MutableStateFlow<Long?>(null)
    private val zoomScale = MutableStateFlow(1f)
    private val isReady = MutableStateFlow(false)

    /** The previous/next video id within the same folder, ordered by filename — the
     * natural "up next" sequence for a folder of episodes/clips (section: transport). */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val adjacentVideoIds = videoRepository.observeById(videoId).flatMapLatest { video ->
        if (video == null) {
            flowOf(null as Long? to null as Long?)
        } else {
            videoRepository.observeByFolder(video.folderPath).map { siblings ->
                val ordered = siblings.sortedBy { it.filename.lowercase() }
                val index = ordered.indexOfFirst { it.id == videoId }
                val previous = if (index > 0) ordered[index - 1].id else null
                val next = if (index in 0 until ordered.lastIndex) ordered[index + 1].id else null
                previous to next
            }
        }
    }

    val uiState: StateFlow<PlayerScreenState> = combine(
        videoRepository.observeById(videoId),
        controller.state,
        pendingResume,
        aspectRatioMode,
        isLocked,
        settingsDataStore.settingsFlow,
        zoomScale,
        isReady,
        adjacentVideoIds,
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val adjacent = flows[8] as Pair<Long?, Long?>
        PlayerScreenState(
            video = flows[0] as Video?,
            playback = flows[1] as PlayerUiState,
            pendingResumePositionMs = flows[2] as Long?,
            aspectRatioMode = flows[3] as AspectRatioMode,
            isLocked = flows[4] as Boolean,
            appSettings = flows[5] as AppSettings,
            zoomScale = flows[6] as Float,
            isReady = flows[7] as Boolean,
            previousVideoId = adjacent.first,
            nextVideoId = adjacent.second,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerScreenState())

    init {
        prepareInitialPlayback()
        startProgressAutosave()
    }

    private fun prepareInitialPlayback() {
        viewModelScope.launch {
            val video = videoRepository.observeById(videoId).filterNotNull().first()
            val settings = settingsDataStore.settingsFlow.first()
            val playbackSettings = historyRepository.getPlaybackSettings(videoId)
            val resumeMs = historyRepository.getResumePosition(videoId)

            aspectRatioMode.value = playbackSettings?.aspectRatioMode
                ?.let { runCatching { AspectRatioMode.valueOf(it) }.getOrNull() } ?: AspectRatioMode.FIT

            if (resumeMs != null && settings.resumePlaybackEnabled) {
                pendingResume.value = resumeMs
            } else {
                startPlayback(video, startPositionMs = 0L, speed = playbackSettings?.playbackSpeed ?: settings.defaultPlaybackSpeed)
            }
            isReady.value = true
        }
    }

    fun onResumeChoice(resume: Boolean) {
        val video = uiState.value.video ?: return
        val position = pendingResume.value ?: 0L
        pendingResume.value = null
        viewModelScope.launch {
            val speed = historyRepository.getPlaybackSettings(videoId)?.playbackSpeed
                ?: settingsDataStore.settingsFlow.first().defaultPlaybackSpeed
            startPlayback(video, startPositionMs = if (resume) position else 0L, speed = speed)
        }
    }

    /** Re-prepares the current item from the start — the "Retry" action on [com.nexora.player.ui.player.components.PlayerErrorOverlay]. */
    fun retry() {
        val video = uiState.value.video ?: return
        viewModelScope.launch {
            val speed = historyRepository.getPlaybackSettings(videoId)?.playbackSpeed ?: settingsDataStore.settingsFlow.first().defaultPlaybackSpeed
            startPlayback(video, startPositionMs = 0L, speed = speed)
        }
    }

    private fun startPlayback(video: Video, startPositionMs: Long, speed: Float) {
        controller.setMediaItem(uri = Uri.parse(video.uri), title = video.filename, startPositionMs = startPositionMs)
        controller.setSpeed(speed)
    }

    private fun startProgressAutosave() {
        viewModelScope.launch {
            while (isActive) {
                delay(5000)
                val state = controller.state.value
                if (state.durationMs > 0) {
                    historyRepository.saveProgress(videoId, state.positionMs, state.durationMs)
                }
            }
        }
    }

    fun saveProgressNow() {
        val state = controller.state.value
        if (state.durationMs > 0) {
            viewModelScope.launch { historyRepository.saveProgress(videoId, state.positionMs, state.durationMs) }
        }
    }

    fun toggleFavorite() {
        val video = uiState.value.video ?: return
        viewModelScope.launch { videoRepository.toggleFavorite(video.id, !video.isFavorite) }
    }

    fun toggleLock() {
        isLocked.value = !isLocked.value
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        aspectRatioMode.value = mode
        viewModelScope.launch {
            val existing = historyRepository.getPlaybackSettings(videoId)
            historyRepository.savePlaybackSettings(
                (existing ?: PlaybackSettingsEntity(videoId = videoId)).copy(aspectRatioMode = mode.name),
            )
        }
    }

    fun setZoom(scale: Float) {
        zoomScale.value = scale.coerceIn(1f, 3f)
    }

    fun setSpeed(speed: Float) {
        controller.setSpeed(speed)
        viewModelScope.launch {
            val existing = historyRepository.getPlaybackSettings(videoId)
            historyRepository.savePlaybackSettings(
                (existing ?: PlaybackSettingsEntity(videoId = videoId)).copy(playbackSpeed = speed),
            )
        }
    }

    fun selectAudioTrack(track: AudioTrackInfo) = controller.selectAudioTrack(track)

    fun selectSubtitleTrack(track: SubtitleTrackInfo?) = controller.selectSubtitleTrack(track)

    fun addExternalSubtitle(uri: Uri, displayLanguage: String) {
        val video = uiState.value.video ?: return
        val mime = guessSubtitleMime(uri.toString())
        val config = MediaItem.SubtitleConfiguration.Builder(uri)
            .setMimeType(mime)
            .setLanguage(displayLanguage)
            .setSelectionFlags(androidx.media3.common.C.SELECTION_FLAG_DEFAULT)
            .build()
        val currentPosition = controller.player.currentPosition
        controller.setMediaItem(
            uri = Uri.parse(video.uri),
            title = video.filename,
            startPositionMs = currentPosition,
            subtitleConfigs = listOf(config),
        )
    }

    private fun guessSubtitleMime(path: String): String = when {
        path.endsWith(".srt", true) -> MimeTypes.APPLICATION_SUBRIP
        path.endsWith(".vtt", true) -> MimeTypes.TEXT_VTT
        path.endsWith(".ssa", true) || path.endsWith(".ass", true) -> MimeTypes.TEXT_SSA
        else -> MimeTypes.APPLICATION_SUBRIP
    }

    override fun onCleared() {
        saveProgressNow()
        super.onCleared()
    }
}
