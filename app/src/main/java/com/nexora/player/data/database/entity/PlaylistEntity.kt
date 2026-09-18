package com.nexora.player.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val coverThumbnailPath: String? = null,
)
