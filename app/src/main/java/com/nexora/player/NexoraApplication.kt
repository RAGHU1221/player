package com.nexora.player

import android.app.Application
import androidx.media3.common.util.UnstableApi
import coil.Coil
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.nexora.player.data.database.AppDatabase
import com.nexora.player.data.datastore.SettingsDataStore
import com.nexora.player.data.repository.HistoryRepository
import com.nexora.player.data.repository.PlaylistRepository
import com.nexora.player.data.repository.VideoRepository
import com.nexora.player.player.PlayerController

/**
 * Manual composition root (no DI framework — keeps the build graph simple and
 * dependency-free, which matters a lot for a project meant to compile the moment
 * it's opened). Every singleton below is created lazily on first access.
 */
@UnstableApi
class NexoraApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val settingsDataStore: SettingsDataStore by lazy { SettingsDataStore(this) }
    val videoRepository: VideoRepository by lazy { VideoRepository(this, database) }
    val playlistRepository: PlaylistRepository by lazy { PlaylistRepository(database, videoRepository) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(database) }

    /**
     * One [PlayerController] (and therefore one ExoPlayer) for the whole process,
     * shared by the full-screen player, the mini-player and [com.nexora.player.player.PlaybackService].
     * That sharing is what lets playback survive navigation, PiP and the
     * notification controls all pointing at the same session.
     */
    var playerController: PlayerController? = null
        private set

    fun getOrCreatePlayerController(): PlayerController {
        return playerController ?: PlayerController(
            context = this,
            preferSoftwareFallback = true,
        ).also { playerController = it }
    }

    fun releasePlayerControllerIfIdle() {
        val controller = playerController ?: return
        if (!controller.player.isPlaying && !controller.state.value.isBuffering) {
            controller.release()
            playerController = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components { add(VideoFrameDecoder.Factory()) }
                .build(),
        )
    }
}
