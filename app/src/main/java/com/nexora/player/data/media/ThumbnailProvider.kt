package com.nexora.player.data.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Two thumbnail paths, matched to how they're consumed:
 *  - [requestFor] builds a Coil [ImageRequest] using coil-video's frame decoder for
 *    every on-screen grid card. Coil owns memory+disk caching and downsampling
 *    (`size(...)`), so we never hold hundreds of full-resolution bitmaps in RAM —
 *    directly satisfying the "efficient thumbnails, no memory blow-up" requirement.
 *  - [generateAndCacheDiskThumbnail] renders a one-off JPEG to app cache for
 *    contexts Coil can't reach directly: the media-session/notification large
 *    icon and the OS share sheet preview.
 */
object ThumbnailProvider {

    private const val GRID_THUMB_WIDTH = 480
    private const val GRID_THUMB_HEIGHT = 270

    fun requestFor(context: Context, uri: Uri): ImageRequest =
        ImageRequest.Builder(context)
            .data(uri)
            .videoFrameMillis(1000)
            .size(Size(GRID_THUMB_WIDTH, GRID_THUMB_HEIGHT))
            .crossfade(150)
            .build()

    suspend fun generateAndCacheDiskThumbnail(context: Context, uri: Uri, videoId: Long): String? =
        withContext(Dispatchers.IO) {
            val cacheDir = File(context.cacheDir, "thumbnails").apply { mkdirs() }
            val outFile = File(cacheDir, "video_$videoId.jpg")
            if (outFile.exists() && outFile.length() > 0) return@withContext outFile.absolutePath

            val retriever = MediaMetadataRetriever()
            return@withContext try {
                retriever.setDataSource(context, uri)
                val frame: Bitmap? = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
                if (frame == null) {
                    null
                } else {
                    val scaled = downscale(frame, GRID_THUMB_WIDTH, GRID_THUMB_HEIGHT)
                    FileOutputStream(outFile).use { out ->
                        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    if (scaled !== frame) scaled.recycle()
                    frame.recycle()
                    outFile.absolutePath
                }
            } catch (t: Throwable) {
                null
            } finally {
                retriever.release()
            }
        }

    private fun downscale(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) return bitmap
        val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height)
        val targetWidth = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /** Clears the on-disk thumbnail cache (Settings > Storage > Clear thumbnail cache). */
    fun clearCache(context: Context) {
        File(context.cacheDir, "thumbnails").deleteRecursively()
    }
}
