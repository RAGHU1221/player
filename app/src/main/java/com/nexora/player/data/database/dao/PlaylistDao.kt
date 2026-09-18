package com.nexora.player.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nexora.player.data.database.entity.PlaylistEntity
import com.nexora.player.data.database.entity.PlaylistItemEntity
import com.nexora.player.data.database.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun observeById(id: Long): Flow<PlaylistEntity?>

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Query(
        """
        SELECT videos.* FROM videos
        INNER JOIN playlist_items ON playlist_items.videoId = videos.id
        WHERE playlist_items.playlistId = :playlistId
        ORDER BY playlist_items.position ASC
        """,
    )
    fun observeVideosInPlaylist(playlistId: Long): Flow<List<VideoEntity>>

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    fun observeItemCount(playlistId: Long): Flow<Int>

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun nextPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeItem(playlistId: Long, videoId: Long)

    @Update
    suspend fun updateItems(items: List<PlaylistItemEntity>)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    suspend fun getItemsOnce(playlistId: Long): List<PlaylistItemEntity>
}
