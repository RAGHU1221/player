package com.nexora.player.data.repository

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.nexora.player.data.database.AppDatabase
import com.nexora.player.data.database.entity.FavoriteEntity
import com.nexora.player.data.database.entity.FolderEntity
import com.nexora.player.data.database.entity.VideoEntity
import com.nexora.player.data.database.entity.WatchHistoryEntity
import com.nexora.player.data.media.MediaProbe
import com.nexora.player.data.media.MediaScanner
import com.nexora.player.data.media.ThumbnailProvider
import com.nexora.player.domain.model.HdrType
import com.nexora.player.domain.model.Video
import com.nexora.player.domain.model.WatchProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

sealed interface DeleteResult {
    data object Success : DeleteResult
    data class RequiresUserConsent(val intentSender: IntentSender) : DeleteResult
    data class Failed(val message: String) : DeleteResult
}

class VideoRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val scanner: MediaScanner = MediaScanner(context),
    private val probe: MediaProbe = MediaProbe(context),
) {
    private val videoDao = database.videoDao()
    private val historyDao = database.watchHistoryDao()
    private val favoriteDao = database.favoriteDao()
    private val folderDao = database.folderDao()

    // ---- Reactive reads (joined video + progress), used directly by every screen's ViewModel ----

    fun observeLibrary(): Flow<List<Video>> =
        combine(videoDao.observeAll(), historyDao.observeAll()) { videos, history ->
            val progressByVideo = history.associateBy { it.videoId }
            videos.map { it.toDomain(progressByVideo[it.id]) }
        }

    fun observeFavorites(): Flow<List<Video>> =
        combine(videoDao.observeFavorites(), historyDao.observeAll()) { videos, history ->
            val progressByVideo = history.associateBy { it.videoId }
            videos.map { it.toDomain(progressByVideo[it.id]) }
        }

    fun observeContinueWatching(limit: Int = 20): Flow<List<Video>> =
        combine(videoDao.observeAll(), historyDao.observeContinueWatching(limit)) { videos, history ->
            val videosById = videos.associateBy { it.id }
            history.mapNotNull { h -> videosById[h.videoId]?.toDomain(h) }
        }

    fun observeByFolder(folderPath: String): Flow<List<Video>> =
        combine(videoDao.observeByFolder(folderPath), historyDao.observeAll()) { videos, history ->
            val progressByVideo = history.associateBy { it.videoId }
            videos.map { it.toDomain(progressByVideo[it.id]) }
        }

    fun observeFolderPaths(): Flow<List<String>> = videoDao.observeFolderPaths()

    fun search(query: String): Flow<List<Video>> =
        combine(videoDao.search(query), historyDao.observeAll()) { videos, history ->
            val progressByVideo = history.associateBy { it.videoId }
            videos.map { it.toDomain(progressByVideo[it.id]) }
        }

    fun observeById(id: Long): Flow<Video?> =
        combine(videoDao.observeById(id), historyDao.observeAll()) { video, history ->
            video?.toDomain(history.find { it.videoId == video.id })
        }

    fun observeManagedFolders(): Flow<List<FolderEntity>> = folderDao.observeAll()

    // ---- Mutations ----

    suspend fun toggleFavorite(videoId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        videoDao.setFavorite(videoId, isFavorite)
        if (isFavorite) {
            favoriteDao.insert(FavoriteEntity(videoId = videoId, addedAtEpochMs = System.currentTimeMillis()))
        } else {
            favoriteDao.removeByVideoId(videoId)
        }
    }

    suspend fun addSafFolder(treeUri: Uri) = withContext(Dispatchers.IO) {
        context.contentResolver.takePersistableUriPermission(
            treeUri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
        val displayName = DocumentFile.fromTreeUri(context, treeUri)?.name ?: treeUri.lastPathSegment.orEmpty()
        folderDao.insert(
            FolderEntity(
                uriString = treeUri.toString(),
                displayName = displayName,
                isSaf = true,
                addedAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun removeFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        folderDao.delete(folder)
    }

    /** Fast pass: index new files from MediaStore + every enabled SAF folder. Safe to call repeatedly. */
    suspend fun scanLibrary(): Int = withContext(Dispatchers.IO) {
        val known = videoDao.getAllUris().toHashSet()
        val discovered = mutableListOf<VideoEntity>()

        scanner.scanMediaStore().forEach { file ->
            if (file.uri.toString() !in known) {
                discovered += VideoEntity(
                    uri = file.uri.toString(),
                    filename = file.filename,
                    folderPath = file.folderPath,
                    sizeBytes = file.sizeBytes,
                    durationMs = file.durationMs,
                    width = file.width,
                    height = file.height,
                    frameRate = 0f,
                    videoCodec = "",
                    audioCodec = null,
                    audioChannels = 0,
                    bitrateBps = 0,
                    hdrType = HdrType.NONE.name,
                    dateAddedEpochMs = file.dateAddedEpochMs,
                    dateModifiedEpochMs = file.dateModifiedEpochMs,
                    thumbnailPath = null,
                )
            }
        }

        folderDao.getEnabled().filter { it.isSaf }.forEach { folder ->
            val treeUri = Uri.parse(folder.uriString)
            val files = runCatching { scanner.scanSafTree(treeUri) }.getOrDefault(emptyList())
            files.forEach { file ->
                if (file.uri.toString() !in known) {
                    discovered += VideoEntity(
                        uri = file.uri.toString(),
                        filename = file.filename,
                        folderPath = file.folderPath,
                        sizeBytes = file.sizeBytes,
                        durationMs = file.durationMs,
                        width = file.width,
                        height = file.height,
                        frameRate = 0f,
                        videoCodec = "",
                        audioCodec = null,
                        audioChannels = 0,
                        bitrateBps = 0,
                        hdrType = HdrType.NONE.name,
                        dateAddedEpochMs = file.dateAddedEpochMs,
                        dateModifiedEpochMs = file.dateModifiedEpochMs,
                        thumbnailPath = null,
                    )
                }
            }
            folderDao.updateVideoCount(folder.id, files.size)
        }

        if (discovered.isNotEmpty()) videoDao.insertAll(discovered)
        discovered.size
    }

    /** Slow pass: fills in codec/HDR/frame-rate/audio-track details, one file at a time. Call from a background worker/coroutine after [scanLibrary]. */
    suspend fun probeUnprocessedVideos(onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }) =
        withContext(Dispatchers.IO) {
            val pending = videoDao.observeAll().first().filter { it.videoCodec.isBlank() }
            pending.forEachIndexed { index, entity ->
                val result = probe.probe(Uri.parse(entity.uri))
                if (result != null) {
                    videoDao.update(
                        entity.copy(
                            videoCodec = result.videoCodec,
                            audioCodec = result.audioCodec,
                            audioChannels = result.audioChannels,
                            bitrateBps = result.bitrateBps,
                            frameRate = result.frameRate,
                            hdrType = result.hdrType.name,
                            subtitleTrackCount = result.subtitleTrackCount,
                            audioTrackCount = result.audioTrackCount,
                            width = if (entity.width == 0) result.width else entity.width,
                            height = if (entity.height == 0) result.height else entity.height,
                        ),
                    )
                }
                val thumb = ThumbnailProvider.generateAndCacheDiskThumbnail(context, Uri.parse(entity.uri), entity.id)
                if (thumb != null) videoDao.setThumbnailPath(entity.id, thumb)
                onProgress(index + 1, pending.size)
            }
        }

    suspend fun deleteVideo(video: Video): DeleteResult = withContext(Dispatchers.IO) {
        val uri = Uri.parse(video.uri)
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            videoDao.deleteById(video.id)
            if (rows > 0) DeleteResult.Success else DeleteResult.Failed("File already removed")
        } catch (e: SecurityException) {
            // Scoped storage (Android 10+): the OS requires explicit user consent to
            // delete media this app doesn't own. Surface the IntentSender so the
            // ViewModel can launch it via ActivityResultContracts.StartIntentSenderForResult.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                DeleteResult.RequiresUserConsent(e.userAction.actionIntent.intentSender)
            } else {
                DeleteResult.Failed(e.message ?: "Permission denied")
            }
        } catch (t: Throwable) {
            DeleteResult.Failed(t.message ?: "Unknown error")
        }
    }

    suspend fun confirmDeleteAfterConsent(video: Video) = withContext(Dispatchers.IO) {
        videoDao.deleteById(video.id)
    }

    suspend fun renameVideo(video: Video, newDisplayName: String) = withContext(Dispatchers.IO) {
        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, newDisplayName)
        }
        runCatching { context.contentResolver.update(Uri.parse(video.uri), values, null, null) }
        videoDao.getById(video.id)?.let { videoDao.update(it.copy(filename = newDisplayName)) }
    }
}

private fun VideoEntity.toDomain(history: WatchHistoryEntity?): Video = Video(
    id = id,
    uri = uri,
    filename = filename,
    folderPath = folderPath,
    sizeBytes = sizeBytes,
    durationMs = durationMs,
    width = width,
    height = height,
    frameRate = frameRate,
    videoCodec = videoCodec,
    audioCodec = audioCodec,
    audioChannels = audioChannels,
    bitrateBps = bitrateBps,
    hdrType = HdrType.fromStored(hdrType),
    dateAddedEpochMs = dateAddedEpochMs,
    dateModifiedEpochMs = dateModifiedEpochMs,
    thumbnailPath = thumbnailPath,
    isFavorite = isFavorite,
    subtitleTrackCount = subtitleTrackCount,
    audioTrackCount = audioTrackCount,
    watchProgress = history?.let {
        WatchProgress(
            positionMs = it.positionMs,
            durationMs = it.durationMs,
            watchedPercent = it.watchedPercent,
            lastPlayedAtEpochMs = it.lastPlayedAtEpochMs,
            completed = it.completed,
        )
    },
)
