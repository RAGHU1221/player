package com.nexora.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per scanned media file. [uri] is the durable content:// (MediaStore) or
 * SAF document URI string — never a raw filesystem path, so playback keeps working
 * across app restarts and on scoped-storage devices.
 */
@Entity(
    tableName = "videos",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["folderPath"]),
        Index(value = ["dateAddedEpochMs"]),
    ],
)
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    /** One of [com.nexora.player.domain.model.HdrType] name(), or "NONE". */
    val hdrType: String,
    val dateAddedEpochMs: Long,
    val dateModifiedEpochMs: Long,
    val thumbnailPath: String?,
    val isFavorite: Boolean = false,
    val subtitleTrackCount: Int = 0,
    val audioTrackCount: Int = 1,
)
