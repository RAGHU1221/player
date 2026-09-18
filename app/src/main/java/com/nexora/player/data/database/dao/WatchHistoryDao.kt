package com.nexora.player.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexora.player.data.database.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY lastPlayedAtEpochMs DESC")
    fun observeAll(): Flow<List<WatchHistoryEntity>>

    /** "Continue Watching": started but not finished, most recent first. */
    @Query(
        "SELECT * FROM watch_history WHERE completed = 0 AND watchedPercent > 0.02 " +
            "ORDER BY lastPlayedAtEpochMs DESC LIMIT :limit",
    )
    fun observeContinueWatching(limit: Int = 20): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getForVideo(videoId: Long): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    fun observeForVideo(videoId: Long): Flow<WatchHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WatchHistoryEntity)

    @Delete
    suspend fun delete(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE videoId = :videoId")
    suspend fun deleteForVideo(videoId: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
