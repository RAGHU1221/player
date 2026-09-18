package com.nexora.player.domain.model

/**
 * UI-facing, fully-joined view of a library item: static file metadata from
 * [com.nexora.player.data.database.entity.VideoEntity] merged with its live
 * watch progress and favorite state. Built by `VideoRepository` via a
 * `combine()` of the three underlying Flows — see data/repository/VideoRepository.kt.
 */
data class Video(
    val id: Long,
    val uri: String,
    val filename: String,
    val folderPath: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val videoCodec: String,
    val audioCodec: String?,
    val audioChannels: Int,
    val bitrateBps: Long,
    val hdrType: HdrType,
    val dateAddedEpochMs: Long,
    val dateModifiedEpochMs: Long,
    val thumbnailPath: String?,
    val isFavorite: Boolean,
    val subtitleTrackCount: Int,
    val audioTrackCount: Int,
    val watchProgress: WatchProgress?,
) {
    val resolution: Resolution get() = Resolution.fromHeight(height)
    val isCompleted: Boolean get() = watchProgress?.completed == true
    val isStarted: Boolean get() = (watchProgress?.watchedPercent ?: 0f) > 0.02f && !isCompleted
}

data class WatchProgress(
    val positionMs: Long,
    val durationMs: Long,
    val watchedPercent: Float,
    val lastPlayedAtEpochMs: Long,
    val completed: Boolean,
)

data class PlaylistSummary(
    val id: Long,
    val name: String,
    val itemCount: Int,
    val coverThumbnailPath: String?,
    val updatedAtEpochMs: Long,
)

data class AudioTrackInfo(
    val trackIndex: Int,
    val groupIndex: Int,
    val trackInGroupIndex: Int,
    val language: String,
    val codec: String,
    val channelCount: Int,
    val isSelected: Boolean,
) {
    val displayLabel: String
        get() {
            val channels = when (channelCount) {
                1 -> "1.0"
                2 -> "2.0"
                6 -> "5.1"
                8 -> "7.1"
                else -> if (channelCount > 0) "${channelCount}ch" else ""
            }
            return listOf(language, codec, channels).filter { it.isNotBlank() }.joinToString(" • ")
        }
}

data class SubtitleTrackInfo(
    val trackIndex: Int,
    val groupIndex: Int,
    val trackInGroupIndex: Int,
    val language: String,
    val format: String,
    val isSelected: Boolean,
)

data class FolderSummary(
    val id: Long,
    val uriString: String,
    val displayName: String,
    val videoCount: Int,
    val isEnabled: Boolean,
)
