package com.nexora.player.data.media

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import androidx.media3.common.MimeTypes
import com.nexora.player.domain.model.HdrType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Everything we know about a file once it's actually opened, beyond what MediaStore gives for free. */
data class ProbeResult(
    val videoCodec: String,
    val audioCodec: String?,
    val audioChannels: Int,
    val bitrateBps: Long,
    val frameRate: Float,
    val hdrType: HdrType,
    val subtitleTrackCount: Int,
    val audioTrackCount: Int,
    val width: Int,
    val height: Int,
)

/**
 * Deep-inspects a media file with [MediaExtractor] to pull codec, frame rate, HDR
 * transfer function and track counts. Deliberately NOT run during the initial
 * library scan (see [MediaScanner]) — MediaStore already supplies width/height/
 * duration fast enough for the grid to render instantly; this runs afterwards,
 * one file at a time, so it never blocks the UI or causes jank on large libraries.
 */
class MediaProbe(private val context: Context) {

    suspend fun probe(uri: Uri): ProbeResult? = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
        } catch (t: Throwable) {
            extractor.release()
            return@withContext null
        }

        var videoCodec = "Unknown"
        var audioCodec: String? = null
        var audioChannels = 0
        var frameRate = 0f
        var width = 0
        var height = 0
        var hdrType = HdrType.NONE
        var audioTrackCount = 0
        var subtitleTrackCount = 0
        var maxBitrate = 0L

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue

            when {
                mime.startsWith("video/") -> {
                    videoCodec = mimeToFriendlyCodec(mime)
                    width = format.safeInt(MediaFormat.KEY_WIDTH, width)
                    height = format.safeInt(MediaFormat.KEY_HEIGHT, height)
                    frameRate = format.safeFloat(MediaFormat.KEY_FRAME_RATE, frameRate)
                    maxBitrate = maxOf(maxBitrate, format.safeInt(MediaFormat.KEY_BIT_RATE, 0).toLong())
                    hdrType = detectHdr(format, mime)
                }
                mime.startsWith("audio/") -> {
                    audioTrackCount++
                    if (audioCodec == null) {
                        audioCodec = mimeToFriendlyCodec(mime)
                        audioChannels = format.safeInt(MediaFormat.KEY_CHANNEL_COUNT, 2)
                    }
                }
                mime.startsWith("text/") || mime.startsWith("application/") -> {
                    if (mime.contains("subrip") || mime.contains("ttml") || mime.contains("vtt") || mime.contains("cea")) {
                        subtitleTrackCount++
                    }
                }
            }
        }

        extractor.release()

        ProbeResult(
            videoCodec = videoCodec,
            audioCodec = audioCodec,
            audioChannels = audioChannels,
            bitrateBps = maxBitrate,
            frameRate = frameRate,
            hdrType = hdrType,
            subtitleTrackCount = subtitleTrackCount,
            audioTrackCount = audioTrackCount.coerceAtLeast(1),
            width = width,
            height = height,
        )
    }

    private fun detectHdr(format: MediaFormat, mime: String): HdrType {
        if (mime == MimeTypes.VIDEO_DOLBY_VISION) return HdrType.DOLBY_VISION

        // KEY_COLOR_TRANSFER values mirror android.media.MediaFormat constants
        // (only resolvable on API 24+; absent on older/undetected streams).
        val transfer = if (format.containsKey(MediaFormat.KEY_COLOR_TRANSFER)) {
            format.getInteger(MediaFormat.KEY_COLOR_TRANSFER)
        } else {
            -1
        }
        val hasHdrStaticInfo = format.containsKey(MediaFormat.KEY_HDR_STATIC_INFO)

        return when {
            transfer == MediaFormat.COLOR_TRANSFER_ST2084 && hasHdrStaticInfo -> HdrType.HDR10
            transfer == MediaFormat.COLOR_TRANSFER_ST2084 -> HdrType.HDR10
            transfer == MediaFormat.COLOR_TRANSFER_HLG -> HdrType.HLG
            else -> HdrType.NONE
        }
    }

    private fun mimeToFriendlyCodec(mime: String): String = when (mime) {
        MimeTypes.VIDEO_H264 -> "H.264/AVC"
        MimeTypes.VIDEO_H265 -> "H.265/HEVC"
        MimeTypes.VIDEO_VP8 -> "VP8"
        MimeTypes.VIDEO_VP9 -> "VP9"
        MimeTypes.VIDEO_AV1 -> "AV1"
        MimeTypes.VIDEO_DOLBY_VISION -> "Dolby Vision"
        MimeTypes.AUDIO_AAC -> "AAC"
        MimeTypes.AUDIO_AC3 -> "AC3"
        MimeTypes.AUDIO_E_AC3 -> "E-AC3"
        MimeTypes.AUDIO_DTS -> "DTS"
        MimeTypes.AUDIO_OPUS -> "Opus"
        MimeTypes.AUDIO_VORBIS -> "Vorbis"
        MimeTypes.AUDIO_MPEG -> "MP3"
        MimeTypes.AUDIO_FLAC -> "FLAC"
        else -> mime.substringAfter('/').uppercase()
    }

    private fun MediaFormat.safeInt(key: String, fallback: Int): Int =
        if (containsKey(key)) getInteger(key) else fallback

    private fun MediaFormat.safeFloat(key: String, fallback: Float): Float =
        if (containsKey(key)) {
            runCatching { getFloat(key) }.getOrElse { getInteger(key).toFloat() }
        } else {
            fallback
        }
}
