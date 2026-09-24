package com.nexora.player.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nexora.player.ui.theme.NexoraThemeVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nexora_settings")

enum class RotationMode { AUTO, PORTRAIT_LOCK, LANDSCAPE_LOCK }

/**
 * App-wide DEFAULTS a new playback session starts from (theme, gesture toggles,
 * default speed/seek amount, decoder preference). Contrasted with
 * `PlaybackSettingsEntity` in Room, which remembers what the user chose for one
 * *specific* video the last time they watched it.
 */
data class AppSettings(
    val themeVariant: NexoraThemeVariant = NexoraThemeVariant.GLOW_DARK,
    val autoplay: Boolean = true,
    val resumePlaybackEnabled: Boolean = true,
    val backgroundAudioEnabled: Boolean = true,
    val doubleTapSeekSeconds: Int = 10,
    val defaultPlaybackSpeed: Float = 1.0f,
    val rememberLastSpeed: Boolean = true,
    val gesturesEnabled: Boolean = true,
    val seekGestureEnabled: Boolean = true,
    val brightnessGestureEnabled: Boolean = true,
    val volumeGestureEnabled: Boolean = true,
    val hardwareAccelerationEnabled: Boolean = true,
    val preferSoftwareFallback: Boolean = true,
    val rotationMode: RotationMode = RotationMode.AUTO,
    val subtitleScale: Float = 1.0f,
    val subtitleColorArgb: Long = 0xFFFFFFFF,
    val subtitleBackgroundEnabled: Boolean = true,
    val subtitleDelayMs: Long = 0,
    val subtitlePositionBottomPercent: Float = 0.08f,
)

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_variant")
        val AUTOPLAY = booleanPreferencesKey("autoplay")
        val RESUME = booleanPreferencesKey("resume_playback")
        val BACKGROUND_AUDIO = booleanPreferencesKey("background_audio")
        val DOUBLE_TAP_SEEK = intPreferencesKey("double_tap_seek_seconds")
        val DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val REMEMBER_SPEED = booleanPreferencesKey("remember_last_speed")
        val GESTURES_ENABLED = booleanPreferencesKey("gestures_enabled")
        val SEEK_GESTURE = booleanPreferencesKey("seek_gesture")
        val BRIGHTNESS_GESTURE = booleanPreferencesKey("brightness_gesture")
        val VOLUME_GESTURE = booleanPreferencesKey("volume_gesture")
        val HW_ACCEL = booleanPreferencesKey("hardware_acceleration")
        val SW_FALLBACK = booleanPreferencesKey("software_fallback")
        val ROTATION_MODE = stringPreferencesKey("rotation_mode")
        val SUB_SCALE = floatPreferencesKey("subtitle_scale")
        val SUB_COLOR = longPreferencesKey("subtitle_color")
        val SUB_BG = booleanPreferencesKey("subtitle_background")
        val SUB_DELAY = longPreferencesKey("subtitle_delay_ms")
        val SUB_POSITION = floatPreferencesKey("subtitle_position")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeVariant = prefs[Keys.THEME]?.let { runCatching { NexoraThemeVariant.valueOf(it) }.getOrNull() }
                ?: NexoraThemeVariant.GLOW_DARK,
            autoplay = prefs[Keys.AUTOPLAY] ?: true,
            resumePlaybackEnabled = prefs[Keys.RESUME] ?: true,
            backgroundAudioEnabled = prefs[Keys.BACKGROUND_AUDIO] ?: true,
            doubleTapSeekSeconds = prefs[Keys.DOUBLE_TAP_SEEK] ?: 10,
            defaultPlaybackSpeed = prefs[Keys.DEFAULT_SPEED] ?: 1.0f,
            rememberLastSpeed = prefs[Keys.REMEMBER_SPEED] ?: true,
            gesturesEnabled = prefs[Keys.GESTURES_ENABLED] ?: true,
            seekGestureEnabled = prefs[Keys.SEEK_GESTURE] ?: true,
            brightnessGestureEnabled = prefs[Keys.BRIGHTNESS_GESTURE] ?: true,
            volumeGestureEnabled = prefs[Keys.VOLUME_GESTURE] ?: true,
            hardwareAccelerationEnabled = prefs[Keys.HW_ACCEL] ?: true,
            preferSoftwareFallback = prefs[Keys.SW_FALLBACK] ?: true,
            rotationMode = prefs[Keys.ROTATION_MODE]?.let { runCatching { RotationMode.valueOf(it) }.getOrNull() }
                ?: RotationMode.AUTO,
            subtitleScale = prefs[Keys.SUB_SCALE] ?: 1.0f,
            subtitleColorArgb = prefs[Keys.SUB_COLOR] ?: 0xFFFFFFFF,
            subtitleBackgroundEnabled = prefs[Keys.SUB_BG] ?: true,
            subtitleDelayMs = prefs[Keys.SUB_DELAY] ?: 0L,
            subtitlePositionBottomPercent = prefs[Keys.SUB_POSITION] ?: 0.08f,
        )
    }

    suspend fun setTheme(variant: NexoraThemeVariant) = update { it[Keys.THEME] = variant.name }
    suspend fun setAutoplay(value: Boolean) = update { it[Keys.AUTOPLAY] = value }
    suspend fun setResumePlaybackEnabled(value: Boolean) = update { it[Keys.RESUME] = value }
    suspend fun setBackgroundAudioEnabled(value: Boolean) = update { it[Keys.BACKGROUND_AUDIO] = value }
    suspend fun setDoubleTapSeekSeconds(value: Int) = update { it[Keys.DOUBLE_TAP_SEEK] = value }
    suspend fun setDefaultPlaybackSpeed(value: Float) = update { it[Keys.DEFAULT_SPEED] = value }
    suspend fun setRememberLastSpeed(value: Boolean) = update { it[Keys.REMEMBER_SPEED] = value }
    suspend fun setGesturesEnabled(value: Boolean) = update { it[Keys.GESTURES_ENABLED] = value }
    suspend fun setSeekGestureEnabled(value: Boolean) = update { it[Keys.SEEK_GESTURE] = value }
    suspend fun setBrightnessGestureEnabled(value: Boolean) = update { it[Keys.BRIGHTNESS_GESTURE] = value }
    suspend fun setVolumeGestureEnabled(value: Boolean) = update { it[Keys.VOLUME_GESTURE] = value }
    suspend fun setHardwareAccelerationEnabled(value: Boolean) = update { it[Keys.HW_ACCEL] = value }
    suspend fun setPreferSoftwareFallback(value: Boolean) = update { it[Keys.SW_FALLBACK] = value }
    suspend fun setRotationMode(mode: RotationMode) = update { it[Keys.ROTATION_MODE] = mode.name }
    suspend fun setSubtitleScale(value: Float) = update { it[Keys.SUB_SCALE] = value }
    suspend fun setSubtitleColor(argb: Long) = update { it[Keys.SUB_COLOR] = argb }
    suspend fun setSubtitleBackgroundEnabled(value: Boolean) = update { it[Keys.SUB_BG] = value }
    suspend fun setSubtitleDelayMs(value: Long) = update { it[Keys.SUB_DELAY] = value }
    suspend fun setSubtitlePosition(value: Float) = update { it[Keys.SUB_POSITION] = value }

    private suspend fun update(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }
}
