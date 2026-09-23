package com.nexora.player.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.nexora.player.domain.model.AudioTrackInfo
import com.nexora.player.domain.model.SubtitleTrackInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class PlayerErrorType {
    UNSUPPORTED_FORMAT, DECODER_UNAVAILABLE, FILE_MISSING, PERMISSION_DENIED, CORRUPTED_MEDIA, NETWORK, UNKNOWN
}

data class PlayerUiError(val type: PlayerErrorType, val message: String)

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val bufferedPositionMs: Long = 0,
    val playbackSpeed: Float = 1f,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val frameRate: Float = 0f,
    val audioTracks: List<AudioTrackInfo> = emptyList(),
    val subtitleTracks: List<SubtitleTrackInfo> = emptyList(),
    val subtitlesEnabled: Boolean = true,
    val error: PlayerUiError? = null,
    val isEnded: Boolean = false,
    val mediaTitle: String = "",
) {
    val is4k: Boolean get() = videoHeight >= 2000 || videoWidth >= 3800
}

/**
 * Thin, StateFlow-first wrapper around a single shared [ExoPlayer]. Every screen
 * that needs playback state (the full player, the mini-player, a future PiP
 * overlay) observes [state] instead of touching the player directly, which is
 * what lets navigation away from [com.nexora.player.ui.player.PlayerScreen] keep
 * audio/video running behind a mini-player with zero duplicated logic.
 */
@UnstableApi
class PlayerController(context: Context, preferSoftwareFallback: Boolean = true) {

    val player: ExoPlayer = ExoPlayerManager.create(context, preferSoftwareFallback)

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickerJob: Job? = null
    private var speedBeforeLongPressBoost: Float? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.value = _state.value.copy(
                isBuffering = playbackState == Player.STATE_BUFFERING,
                isEnded = playbackState == Player.STATE_ENDED,
                durationMs = player.duration.coerceAtLeast(0),
            )
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(error = error.toUiError(), isBuffering = false)
        }

        override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
            _state.value = _state.value.copy(
                videoWidth = videoSize.width,
                videoHeight = videoSize.height,
                frameRate = if (videoSize.height > 0) _state.value.frameRate else _state.value.frameRate,
            )
        }

        override fun onTracksChanged(tracks: Tracks) {
            _state.value = _state.value.copy(
                audioTracks = tracks.toAudioTrackInfos(),
                subtitleTracks = tracks.toSubtitleTrackInfos(),
            )
        }

        override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
            _state.value = _state.value.copy(playbackSpeed = playbackParameters.speed)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _state.value = _state.value.copy(
                mediaTitle = mediaItem?.mediaMetadata?.title?.toString().orEmpty(),
                error = null,
            )
        }
    }

    init {
        player.addListener(playerListener)
        startPositionTicker()
    }

    private fun startPositionTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                if (player.playbackState != Player.STATE_IDLE) {
                    _state.value = _state.value.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0),
                        bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0),
                        durationMs = player.duration.coerceAtLeast(0),
                    )
                }
                kotlinx.coroutines.delay(400)
            }
        }
    }

    fun setMediaItem(
        uri: Uri,
        title: String,
        startPositionMs: Long = 0,
        subtitleConfigs: List<MediaItem.SubtitleConfiguration> = emptyList(),
        playWhenReady: Boolean = true,
    ) {
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(androidx.media3.common.MediaMetadata.Builder().setTitle(title).build())
            .setSubtitleConfigurations(subtitleConfigs)
            .build()
        player.setMediaItem(mediaItem, startPositionMs)
        player.playWhenReady = playWhenReady
        player.prepare()
        _state.value = _state.value.copy(error = null, mediaTitle = title, isEnded = false)
    }

    fun playPause() {
        if (player.playbackState == Player.STATE_ENDED) {
            player.seekTo(0)
            player.play()
            return
        }
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekTo(positionMs: Long) = player.seekTo(positionMs.coerceIn(0, player.duration.coerceAtLeast(0)))

    fun seekForward(seconds: Int) = seekTo(player.currentPosition + seconds * 1000L)

    fun seekBackward(seconds: Int) = seekTo(player.currentPosition - seconds * 1000L)

    fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    fun beginLongPressSpeedBoost() {
        if (speedBeforeLongPressBoost == null) {
            speedBeforeLongPressBoost = player.playbackParameters.speed
            player.setPlaybackSpeed(2.0f)
        }
    }

    fun endLongPressSpeedBoost() {
        speedBeforeLongPressBoost?.let { player.setPlaybackSpeed(it) }
        speedBeforeLongPressBoost = null
    }

    fun selectAudioTrack(track: AudioTrackInfo) {
        val group = player.currentTracks.groups.getOrNull(track.groupIndex) ?: return
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setOverrideForType(
                TrackSelectionOverride(group.mediaTrackGroup, track.trackInGroupIndex),
            )
            .build()
    }

    fun selectSubtitleTrack(track: SubtitleTrackInfo?) {
        val params = player.trackSelectionParameters.buildUpon()
        if (track == null) {
            params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            _state.value = _state.value.copy(subtitlesEnabled = false)
        } else {
            val group = player.currentTracks.groups.getOrNull(track.groupIndex) ?: return
            params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, track.trackInGroupIndex))
            _state.value = _state.value.copy(subtitlesEnabled = true)
        }
        player.trackSelectionParameters = params.build()
    }

    fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    /** Used by the mini-player's close button: stops playback and clears the loaded item so the mini-player disappears. */
    fun stopAndClear() {
        player.stop()
        player.clearMediaItems()
    }

    fun release() {
        tickerJob?.cancel()
        player.removeListener(playerListener)
        player.release()
    }
}

private fun Tracks.toAudioTrackInfos(): List<AudioTrackInfo> {
    val results = mutableListOf<AudioTrackInfo>()
    groups.forEachIndexed { groupIndex, group ->
        if (group.type != C.TRACK_TYPE_AUDIO) return@forEachIndexed
        for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            results += AudioTrackInfo(
                trackIndex = results.size,
                groupIndex = groupIndex,
                trackInGroupIndex = i,
                language = format.language?.uppercase() ?: "Und",
                codec = format.sampleMimeType?.substringAfter('/')?.uppercase() ?: "",
                channelCount = format.channelCount,
                isSelected = group.isTrackSelected(i),
            )
        }
    }
    return results
}

private fun Tracks.toSubtitleTrackInfos(): List<SubtitleTrackInfo> {
    val results = mutableListOf<SubtitleTrackInfo>()
    groups.forEachIndexed { groupIndex, group ->
        if (group.type != C.TRACK_TYPE_TEXT) return@forEachIndexed
        for (i in 0 until group.length) {
            val format = group.getTrackFormat(i)
            results += SubtitleTrackInfo(
                trackIndex = results.size,
                groupIndex = groupIndex,
                trackInGroupIndex = i,
                language = format.language?.uppercase() ?: "Und",
                format = format.sampleMimeType?.substringAfter('/')?.uppercase() ?: "SUB",
                isSelected = group.isTrackSelected(i),
            )
        }
    }
    return results
}

private fun PlaybackException.toUiError(): PlayerUiError = when (errorCode) {
    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
    PlaybackException.ERROR_CODE_IO_NO_PERMISSION,
    -> PlayerUiError(PlayerErrorType.FILE_MISSING, "This file could not be found. It may have been moved or deleted.")

    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
    PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
    -> PlayerUiError(PlayerErrorType.DECODER_UNAVAILABLE, "This device has no decoder for this codec.")

    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED,
    -> PlayerUiError(PlayerErrorType.CORRUPTED_MEDIA, "This file appears to be corrupted and can't be played.")

    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
    -> PlayerUiError(PlayerErrorType.NETWORK, "Network error. Check your connection and try again.")

    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
    -> PlayerUiError(
        PlayerErrorType.UNSUPPORTED_FORMAT,
        "Unable to play this video on this device.\nTry another decoder or format.",
    )

    else -> PlayerUiError(PlayerErrorType.UNKNOWN, "Playback error. Please try again.")
}
