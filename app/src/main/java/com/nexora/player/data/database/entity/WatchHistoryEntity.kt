package com.nexora.player.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Resume position + progress for a video. One row per video (upserted on every playback tick). */
@Entity(
    tableName = "watch_history",
    foreignKeys = [
        ForeignKey(
            entity = VideoEntity::class,
            parentColumns = ["id"],
            childColumns = ["videoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["videoId"], unique = true), Index(value = ["lastPlayedAtEpochMs"])],
)
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: Long,
    val positionMs: Long,
    val durationMs: Long,
    val watchedPercent: Float,
    val lastPlayedAtEpochMs: Long,
    val completed: Boolean,
)
