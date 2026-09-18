package com.nexora.player.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A user-selected SAF tree (or a MediaStore bucket path) that the scanner watches. */
@Entity(tableName = "folders", indices = [Index(value = ["uriString"], unique = true)])
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val displayName: String,
    val isSaf: Boolean,
    val addedAtEpochMs: Long,
    val videoCount: Int = 0,
    val isEnabled: Boolean = true,
)
