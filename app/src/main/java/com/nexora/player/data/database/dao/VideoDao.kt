package com.nexora.player.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nexora.player.data.database.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Query("SELECT * FROM videos ORDER BY dateAddedEpochMs DESC")
    fun observeAll(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE folderPath = :folderPath ORDER BY filename ASC")
    fun observeByFolder(folderPath: String): Flow<List<VideoEntity>>

    @Query("SELECT DISTINCT folderPath FROM videos ORDER BY folderPath ASC")
    fun observeFolderPaths(): Flow<List<String>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY dateAddedEpochMs DESC")
    fun observeFavorites(): Flow<List<VideoEntity>>

    @Query(
        """
        SELECT * FROM videos
        WHERE (:query = '' OR filename LIKE '%' || :query || '%' OR folderPath LIKE '%' || :query || '%')
        ORDER BY filename ASC
        """,
    )
    fun search(query: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getById(id: Long): VideoEntity?

    @Query("SELECT * FROM videos WHERE id = :id")
    fun observeById(id: Long): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): VideoEntity?

    @Query("SELECT uri FROM videos")
    suspend fun getAllUris(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(video: VideoEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(videos: List<VideoEntity>): List<Long>

    @Update
    suspend fun update(video: VideoEntity)

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE videos SET thumbnailPath = :path WHERE id = :id")
    suspend fun setThumbnailPath(id: Long, path: String)

    @Delete
    suspend fun delete(video: VideoEntity)

    @Query("DELETE FROM videos WHERE uri IN (:uris)")
    suspend fun deleteByUris(uris: List<String>)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM videos")
    fun observeCount(): Flow<Int>
}
