package com.nexora.player.data.media

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Cheap, MediaStore/DocumentFile-only metadata — no codec probing, so scanning stays fast. */
data class ScannedFile(
    val uri: Uri,
    val filename: String,
    val folderPath: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val dateAddedEpochMs: Long,
    val dateModifiedEpochMs: Long,
)

private val SUPPORTED_EXTENSIONS = setOf(
    "mp4", "mkv", "webm", "mov", "avi", "m4v", "3gp", "ts", "mpeg", "mpg", "flv",
)

/**
 * Two independent sources feed the library, matching the spec's "MediaStore where
 * appropriate + SAF for user-picked folders" requirement:
 *  - [scanMediaStore] queries the system video index (fast, covers the common case,
 *    needs only READ_MEDIA_VIDEO — no broad storage permission).
 *  - [scanSafTree] walks a folder the user explicitly granted via the Storage
 *    Access Framework (works for folders MediaStore doesn't index, e.g. some SD
 *    cards / app-specific export folders).
 */
class MediaScanner(private val context: Context) {

    suspend fun scanMediaStore(): List<ScannedFile> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ScannedFile>()
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.RELATIVE_PATH,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED,
        )

        context.contentResolver.query(collection, projection, null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC")
            ?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val filename = cursor.getString(nameCol) ?: continue
                    if (SUPPORTED_EXTENSIONS.none { filename.endsWith(".$it", ignoreCase = true) }) continue

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    results += ScannedFile(
                        uri = contentUri,
                        filename = filename,
                        folderPath = cursor.getString(pathCol) ?: "Unknown",
                        sizeBytes = cursor.getLong(sizeCol),
                        durationMs = cursor.getLong(durationCol),
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol),
                        dateAddedEpochMs = cursor.getLong(dateAddedCol) * 1000,
                        dateModifiedEpochMs = cursor.getLong(dateModifiedCol) * 1000,
                    )
                }
            }
        results
    }

    suspend fun scanSafTree(treeUri: Uri): List<ScannedFile> = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val results = mutableListOf<ScannedFile>()
        walk(root, root.name.orEmpty(), results)
        results
    }

    private fun walk(dir: DocumentFile, folderPath: String, out: MutableList<ScannedFile>) {
        for (child in dir.listFiles()) {
            if (child.isDirectory) {
                walk(child, "$folderPath/${child.name}", out)
                continue
            }
            val name = child.name ?: continue
            if (SUPPORTED_EXTENSIONS.none { name.endsWith(".$it", ignoreCase = true) }) continue

            out += ScannedFile(
                uri = child.uri,
                filename = name,
                folderPath = folderPath,
                sizeBytes = child.length(),
                durationMs = 0L, // filled in by MediaProbe on first open
                width = 0,
                height = 0,
                dateAddedEpochMs = child.lastModified(),
                dateModifiedEpochMs = child.lastModified(),
            )
        }
    }
}
