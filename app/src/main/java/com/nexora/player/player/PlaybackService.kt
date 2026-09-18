package com.nexora.player.player

import android.content.Intent
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.nexora.player.NexoraApplication

/**
 * Keeps playback (and its lock-screen/Bluetooth/notification controls) alive
 * once the user navigates away from [com.nexora.player.ui.player.PlayerScreen] —
 * background audio (section 17) and the foreground-service requirement that
 * comes with it. Binds to the *same* [PlayerController]/ExoPlayer instance the
 * UI uses via [NexoraApplication], so there's exactly one playback session for
 * the whole app.
 */
@UnstableApi
class PlaybackService : MediaSessionService() {

    private var mediaSessionManager: MediaSessionManager? = null

    override fun onCreate() {
        super.onCreate()
        val app = application as NexoraApplication
        val controller = app.getOrCreatePlayerController()
        mediaSessionManager = MediaSessionManager(this, controller.player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSessionManager?.session

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Swiping the app away from Recents while paused should stop the
        // foreground service; while playing, background audio (section 17)
        // keeps it alive so playback continues like a music app.
        val player = mediaSessionManager?.session?.player
        if (player == null || !player.playWhenReady) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSessionManager?.release()
        mediaSessionManager = null
        super.onDestroy()
    }
}
