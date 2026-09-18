package com.nexora.player.util

import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow

object FormatUtils {

    /** 65 -> "01:05", 3725 * 1000L -> "01:02:05" */
    fun formatDuration(ms: Long): String {
        if (ms <= 0) return "00:00"
        val totalSeconds = ms / 1000
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceIn(1, units.size)
        val value = bytes / 1024.0.pow(exp.toDouble())
        return String.format(Locale.US, "%.1f %s", value, units[exp - 1])
    }

    fun formatBitrate(bps: Long): String {
        if (bps <= 0) return "—"
        val mbps = bps / 1_000_000.0
        return if (mbps >= 1) String.format(Locale.US, "%.1f Mbps", mbps) else "${bps / 1000} kbps"
    }

    fun formatFrameRate(fps: Float): String =
        if (fps <= 0f) "—" else String.format(Locale.US, "%.2f FPS", fps).replace(".00", "")

    fun formatRelativeDate(epochMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMs
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            days < 30 -> "${days / 7}w ago"
            else -> "${days / 30}mo ago"
        }
    }

    fun remainingTime(positionMs: Long, durationMs: Long): String {
        val remaining = (durationMs - positionMs).coerceAtLeast(0)
        return "-" + formatDuration(remaining)
    }
}
