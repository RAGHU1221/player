package com.nexora.player.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.nexora.player.domain.model.AspectRatioMode
import com.nexora.player.ui.theme.nexoraColors

/**
 * Wraps Media3's [PlayerView] (which internally renders through a hardware
 * [android.view.SurfaceView] for efficient 4K/HDR frame delivery — see section 6)
 * for use inside Compose. All our own chrome (controls, gestures, badges) is
 * layered on top in Compose; PlayerView's own built-in control bar is disabled.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    exoPlayer: ExoPlayer,
    aspectRatioMode: AspectRatioMode,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.nexoraColors.gradientBottom
    AndroidView(
        modifier = modifier.fillMaxSize().background(backgroundColor),
        factory = { context ->
            PlayerView(context).apply {
                player = exoPlayer
                useController = false // Compose renders every control; PlayerView is surface-only.
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                setKeepContentOnPlayerReset(true)
            }
        },
        update = { view ->
            view.player = exoPlayer
            view.resizeMode = aspectRatioMode.toResizeMode()
        },
    )
}

private fun AspectRatioMode.toResizeMode(): Int = when (this) {
    AspectRatioMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
    AspectRatioMode.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
    AspectRatioMode.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
    AspectRatioMode.RATIO_16_9 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    AspectRatioMode.RATIO_4_3 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    AspectRatioMode.ORIGINAL -> AspectRatioFrameLayout.RESIZE_MODE_FIT
}
