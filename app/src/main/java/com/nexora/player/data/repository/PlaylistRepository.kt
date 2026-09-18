package com.nexora.player.data.repository

import com.nexora.player.data.database.AppDatabase
import com.nexora.player.data.database.entity.PlaylistEntity
import com.nexora.player.data.database.entity.PlaylistItemEntity
import com.nexora.player.domain.model.PlaylistSummary
import com.nexora.player.domain.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class PlaylistRepository(database: AppDatabase, private val videoRepository: VideoRepository) {
    private val playlistDao = database.playlistDao()

    fun observePlaylists(): Flow<List<PlaylistSummary>> = playlistDao.observeAll().let { flow ->
        // itemCount is fetched lazily per playlist by the UI (see PlaylistViewModel) to
        // avoid an N+1 Flow<Flow<>> here; this keeps the summary flow cheap and simple.
        kotlinx.coroutines.flow.flow {
            flow.collect { playlists ->
                emit(playlists.map { it.toSummary(itemCount = 0) })
            }
        }
    }

    fun observeItemCount(playlistId: Long): Flow<Int> = playlistDao.observeItemCount(playlistId)

    fun observeVideosInPlaylist(playlistId: Long): Flow<List<Video>> =
        combine(playlistDao.observeVideosInPlaylist(playlistId), videoRepository.observeLibrary()) { items, all ->
            val allById = all.associateBy { it.id }
            items.mapNotNull { allById[it.id] }
        }

    suspend fun createPlaylist(name: String): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        playlistDao.insert(PlaylistEntity(name = name, createdAtEpochMs = now, updatedAtEpochMs = now))
    }

    suspend fun renamePlaylist(playlist: PlaylistEntity, newName: String) = withContext(Dispatchers.IO) {
        playlistDao.update(playlist.copy(name = newName, updatedAtEpochMs = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) = withContext(Dispatchers.IO) {
        playlistDao.delete(playlist)
    }

    suspend fun addVideo(playlistId: Long, videoId: Long) = withContext(Dispatchers.IO) {
        val position = playlistDao.nextPosition(playlistId)
        playlistDao.addItem(PlaylistItemEntity(playlistId = playlistId, videoId = videoId, position = position))
    }

    suspend fun removeVideo(playlistId: Long, videoId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeItem(playlistId, videoId)
    }

    suspend fun reorder(playlistId: Long, orderedVideoIds: List<Long>) = withContext(Dispatchers.IO) {
        val items = playlistDao.getItemsOnce(playlistId).associateBy { it.videoId }
        val updated = orderedVideoIds.mapIndexedNotNull { index, videoId ->
            items[videoId]?.copy(position = index)
        }
        if (updated.isNotEmpty()) playlistDao.updateItems(updated)
    }
}

private fun PlaylistEntity.toSummary(itemCount: Int) = PlaylistSummary(
    id = id,
    name = name,
    itemCount = itemCount,
    coverThumbnailPath = coverThumbnailPath,
    updatedAtEpochMs = updatedAtEpochMs,
)
