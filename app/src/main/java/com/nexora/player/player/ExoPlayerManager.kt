package com.nexora.player.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

/**
 * Builds a single, correctly-configured [ExoPlayer] instance. All of the "4K /
 * HDR / codec" requirements in the spec live here, at the ExoPlayer construction
 * boundary, rather than being scattered across the UI:
 *  - hardware decoding is preferred (EXTENSION_RENDERER_MODE_ON only *adds*
 *    software extension decoders as a fallback, it never disables the hardware
 *    path — ExoPlayer always tries hardware codecs first);
 *  - [DefaultTrackSelector] is left free to pick the highest resolution/bitrate
 *    track a device's decoders actually report support for, so a 4K source is
 *    never pre-emptively downscaled;
 *  - buffer sizes are widened for large 4K/HEVC/AV1 files so seeking and startup
 *    stay smooth without holding excessive memory on low-RAM devices.
 */
@UnstableApi
object ExoPlayerManager {

    fun create(context: Context, preferSoftwareFallback: Boolean): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context.applicationContext).apply {
            setExtensionRendererMode(
                if (preferSoftwareFallback) {
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON
                } else {
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
                },
            )
            setEnableDecoderFallback(true)
        }

        val trackSelector = DefaultTrackSelector(context.applicationContext).apply {
            setParameters(
                buildUponParameters()
                    .setForceHighestSupportedBitrate(false)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowVideoNonSeamlessAdaptiveness(true)
                    .setExceedVideoConstraintsIfNecessary(true)
                    .setExceedRendererCapabilitiesIfNecessary(true),
            )
        }

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 15_000,
                /* maxBufferMs = */ 50_000,
                /* bufferForPlaybackMs = */ 1_500,
                /* bufferForPlaybackAfterRebufferMs = */ 3_000,
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context.applicationContext)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .setHandleAudioBecomingNoisy(true) // pause on headphone unplug
            .build()
    }
}
