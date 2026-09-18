package com.nexora.player.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession

/**
 * Owns the [MediaSession] that turns raw ExoPlayer playback into something the
 * rest of Android understands: lock-screen transport controls, Bluetooth/
 * headset buttons, the notification, and Assistant/quick-settings media
 * controls all talk to the app through this session rather than to ExoPlayer
 * directly. Created and held by [PlaybackService].
 *
 * The default [MediaSession.Builder] already grants a connecting controller
 * (the system notification, Android Auto, Bluetooth, etc.) every standard
 * transport command backed by the player, which is all play/pause/seek/skip/
 * lock-screen control needs — a custom [MediaSession.Callback] is only worth
 * adding once a *custom* action (e.g. a "favorite" notification button) is
 * required, so this stays intentionally minimal rather than reimplementing
 * default behavior.
 */
@UnstableApi
class MediaSessionManager(context: Context, player: ExoPlayer) {

    val session: MediaSession = MediaSession.Builder(context, player).build()

    fun release() {
        session.release()
    }
}
