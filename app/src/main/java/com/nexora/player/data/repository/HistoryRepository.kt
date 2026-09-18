package com.nexora.player.data.repository

import com.nexora.player.data.database.AppDatabase
import com.nexora.player.data.database.entity.WatchHistoryEntity

/** Resume-playback bookkeeping. [saveProgress] is called every few seconds during playback and once more on pause/stop. */
class HistoryRepository(database: AppDatabase) {
    private val historyDao = database.watchHistoryDao()
    private val playbackSettingsDao = database.playbackSettingsDao()

    companion object {
        /** Videos are marked "completed" once watched past this point, mirroring common player heuristics (trailing credits). */
        const val COMPLETED_THRESHOLD = 0.95f
        const val MIN_TRACKABLE_PERCENT = 0.02f
    }

    suspend fun saveProgress(videoId: Long, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val percent = (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        if (percent < MIN_TRACKABLE_PERCENT) return
        historyDao.upsert(
            WatchHistoryEntity(
                videoId = videoId,
                positionMs = positionMs,
                durationMs = durationMs,
                watchedPercent = percent,
                lastPlayedAtEpochMs = System.currentTimeMillis(),
                completed = percent >= COMPLETED_THRESHOLD,
            ),
        )
    }

    suspend fun getResumePosition(videoId: Long): Long? {
        val entry = historyDao.getForVideo(videoId) ?: return null
        if (entry.completed) return null
        return entry.positionMs.takeIf { it > 2_000L }
    }

    suspend fun clearProgress(videoId: Long) = historyDao.deleteForVideo(videoId)

    suspend fun clearAllHistory() = historyDao.clearAll()

    suspend fun getPlaybackSettings(videoId: Long) = playbackSettingsDao.getForVideo(videoId)

    suspend fun savePlaybackSettings(settings: com.nexora.player.data.database.entity.PlaybackSettingsEntity) =
        playbackSettingsDao.upsert(settings)
}
