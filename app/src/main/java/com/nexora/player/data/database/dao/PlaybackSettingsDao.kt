package com.nexora.player.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexora.player.data.database.entity.PlaybackSettingsEntity

@Dao
interface PlaybackSettingsDao {

    @Query("SELECT * FROM playback_settings WHERE videoId = :videoId")
    suspend fun getForVideo(videoId: Long): PlaybackSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: PlaybackSettingsEntity)
}
