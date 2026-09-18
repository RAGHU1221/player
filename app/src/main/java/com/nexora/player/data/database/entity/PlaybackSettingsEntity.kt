package com.nexora.player.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Per-video playback memory: the audio/subtitle track, speed, aspect ratio and
 * subtitle timing/style the user last chose *for this specific file*. Distinct
 * from the app-wide defaults in [com.nexora.player.data.datastore.SettingsDataStore]
 * (DataStore holds "what should a new video start with", Room holds "what did the
 * user choose for this one before").
 */
@Entity(
    tableName = "playback_settings",
    foreignKeys = [
        ForeignKey(
            entity = VideoEntity::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class PlaybackSettingsEntity(
    @PrimaryKey val videoId: Long,
    val lastAudioTrackIndex: Int = -1,
    val lastSubtitleTrackIndex: Int = -1,
    val subtitlesEnabled: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: String = "FIT",
    val subtitleDelayMs: Long = 0,
    val zoomScale: Float = 1.0f,
)
