package com.nexora.player.ui.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import com.nexora.player.data.datastore.RotationMode
import com.nexora.player.domain.model.AspectRatioMode
import com.nexora.player.domain.model.AudioTrackInfo
import com.nexora.player.domain.model.SubtitleTrackInfo
import com.nexora.player.ui.player.components.AspectRatioSheet
import com.nexora.player.ui.player.components.AudioTrackSheet
import com.nexora.player.ui.player.components.ControlsOverlay
import com.nexora.player.ui.player.components.GestureOverlay
import com.nexora.player.ui.player.components.LockedOverlay
import com.nexora.player.ui.player.components.PlayerErrorOverlay
import com.nexora.player.ui.player.components.PlayerSurface
import com.nexora.player.ui.player.components.ResumeDialog
import com.nexora.player.ui.player.components.SpeedSheet
import com.nexora.player.ui.player.components.SubtitleStyleSheet
import com.nexora.player.ui.player.components.SubtitleTrackSheet
import com.nexora.player.ui.theme.nexoraColors
import com.nexora.player.util.GenericViewModelFactory
import com.nexora.player.util.nexoraApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ActiveSheet { NONE, SPEED, AUDIO, SUBTITLE, SUBTITLE_STYLE, ASPECT_RATIO }

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(videoId: Long, onBack: () -> Unit, onOpenDetails: (Long) -> Unit) {
    val context = LocalContext.current
    val app = context.nexoraApp()
    val activity = context as? Activity

    // Lets Next/Previous swap which video this screen plays without leaving the
    // player screen — reuses the existing "one PlayerViewModel per id" pattern below.
    var currentVideoId by rememberSaveable(videoId) { mutableStateOf(videoId) }

    val viewModel: PlayerViewModel = viewModel(
        key = "player_$currentVideoId",
        factory = GenericViewModelFactory {
            PlayerViewModel(
                videoId = currentVideoId,
                videoRepository = app.videoRepository,
                historyRepository = app.historyRepository,
                settingsDataStore = app.settingsDataStore,
                controller = app.getOrCreatePlayerController(),
            )
        },
    )
    val state by viewModel.uiState.collectAsState()

    var controlsVisible by remember { mutableStateOf(true) }
    var activeSheet by remember { mutableStateOf(ActiveSheet.NONE) }
    var brightness by remember { mutableStateOf(0.5f) }
    val coroutineScope = rememberCoroutineScope()

    // ---- System chrome: immersive fullscreen + orientation, matches sections 9 & 26 ----
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = activity?.window
        val originalOrientation = activity?.requestedOrientation
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowInsetsControllerCompat(window, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        onDispose {
            if (window != null) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
                WindowInsetsControllerCompat(window, view).show(WindowInsetsCompat.Type.systemBars())
            }
            activity?.requestedOrientation = originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // AUTO always opens the player in portrait first (regardless of the video's own
    // shape or which way the phone happens to be held), then — once playback actually
    // starts — hands orientation control over to the device sensor, so the user can
    // freely turn the phone to landscape (or back to portrait) at any point while
    // watching. PORTRAIT_LOCK/LANDSCAPE_LOCK stay hard-locked, as before.
    LaunchedEffect(state.appSettings.rotationMode) {
        activity?.requestedOrientation = when (state.appSettings.rotationMode) {
            RotationMode.PORTRAIT_LOCK -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            RotationMode.LANDSCAPE_LOCK -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            RotationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    LaunchedEffect(state.appSettings.rotationMode, state.playback.isPlaying) {
        if (state.appSettings.rotationMode == RotationMode.AUTO && state.playback.isPlaying) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }

    // ---- Pause when backgrounded unless background audio is enabled (sections 17 & 27) ----
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state.appSettings.backgroundAudioEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val isInPip = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && activity?.isInPictureInPictureMode == true
                if (!state.appSettings.backgroundAudioEnabled && !isInPip) {
                    viewModel.controller.player.pause()
                }
                viewModel.saveProgressNow()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ---- Auto-hide controls ----
    LaunchedEffect(controlsVisible, state.playback.isPlaying) {
        if (controlsVisible && state.playback.isPlaying && activeSheet == ActiveSheet.NONE) {
            delay(4000)
            controlsVisible = false
        }
    }

    BackHandler(enabled = true) {
        viewModel.saveProgressNow()
        onBack()
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.addExternalSubtitle(it, "External") }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.nexoraColors.gradientBottom)) {
        PlayerSurface(
            exoPlayer = viewModel.controller.player,
            aspectRatioMode = state.aspectRatioMode,
            modifier = Modifier.fillMaxSize(),
        )

        if (!state.isLocked) {
            GestureOverlay(
                enabled = activeSheet == ActiveSheet.NONE && state.pendingResumePositionMs == null,
                currentBrightness = brightness,
                currentVolume = viewModel.controller.player.volume,
                doubleTapSeekSeconds = state.appSettings.doubleTapSeekSeconds,
                onSingleTap = { controlsVisible = !controlsVisible },
                onDoubleTapSeek = { forward ->
                    if (forward) viewModel.controller.seekForward(state.appSettings.doubleTapSeekSeconds)
                    else viewModel.controller.seekBackward(state.appSettings.doubleTapSeekSeconds)
                },
                onDoubleTapCenter = { viewModel.controller.playPause() },
                onBrightnessChange = { value ->
                    brightness = value
                    activity?.window?.let { w ->
                        val attrs = w.attributes
                        attrs.screenBrightness = value
                        w.attributes = attrs
                    }
                },
                onVolumeChange = { value -> viewModel.controller.setVolume(value) },
                onSeekDrag = { deltaMs ->
                    val target = (viewModel.controller.player.currentPosition + deltaMs)
                        .coerceIn(0, viewModel.controller.player.duration.coerceAtLeast(0))
                    viewModel.controller.seekTo(target)
                },
                onSeekDragCommit = {},
                onZoom = { factor -> viewModel.setZoom(state.zoomScale * factor) },
                onLongPressStart = { viewModel.controller.beginLongPressSpeedBoost() },
                onLongPressEnd = { viewModel.controller.endLongPressSpeedBoost() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        ControlsOverlay(
            visible = controlsVisible && !state.isLocked && state.pendingResumePositionMs == null && state.playback.error == null,
            title = state.video?.filename.orEmpty(),
            videoUri = state.video?.uri.orEmpty(),
            playback = state.playback,
            is4kBadgeVisible = state.playback.is4k,
            codecLabel = state.video?.videoCodec.orEmpty(),
            onBack = { viewModel.saveProgressNow(); onBack() },
            onMore = { state.video?.let { onOpenDetails(it.id) } },
            onPlayPause = { viewModel.controller.playPause() },
            onSeekBack = { viewModel.controller.seekBackward(10) },
            onSeekForward = { viewModel.controller.seekForward(10) },
            onSeekPreview = { ms -> viewModel.controller.seekTo(ms) },
            onSeekCommit = { ms -> viewModel.controller.seekTo(ms) },
            onLock = { viewModel.toggleLock() },
            onSubtitleClick = { activeSheet = ActiveSheet.SUBTITLE },
            onAudioClick = { activeSheet = ActiveSheet.AUDIO },
            onSpeedClick = { activeSheet = ActiveSheet.SPEED },
            onAspectRatioClick = { activeSheet = ActiveSheet.ASPECT_RATIO },
            onSettingsClick = { activeSheet = ActiveSheet.SUBTITLE_STYLE },
            onPipClick = { activity?.enterNexoraPip() },
            hasPrevious = state.previousVideoId != null,
            hasNext = state.nextVideoId != null,
            onPrevious = {
                state.previousVideoId?.let { id ->
                    viewModel.saveProgressNow()
                    currentVideoId = id
                }
            },
            onNext = {
                state.nextVideoId?.let { id ->
                    viewModel.saveProgressNow()
                    currentVideoId = id
                }
            },
            modifier = Modifier.fillMaxSize(),
        )

        if (state.isLocked) {
            LockedOverlay(onUnlock = { viewModel.toggleLock() }, modifier = Modifier.fillMaxSize())
        }

        state.pendingResumePositionMs?.let { resumeMs ->
            ResumeDialog(
                resumePositionMs = resumeMs,
                onResume = { viewModel.onResumeChoice(true) },
                onStartOver = { viewModel.onResumeChoice(false) },
            )
        }

        state.playback.error?.let { error ->
            PlayerErrorOverlay(
                error = error,
                onRetry = { viewModel.retry() },
                onBack = onBack,
            )
        }

        when (activeSheet) {
            ActiveSheet.SPEED -> SpeedSheet(
                currentSpeed = state.playback.playbackSpeed,
                onSelect = { viewModel.setSpeed(it) },
                onDismiss = { activeSheet = ActiveSheet.NONE },
            )
            ActiveSheet.AUDIO -> AudioTrackSheet(
                tracks = state.playback.audioTracks,
                onSelect = { track: AudioTrackInfo -> viewModel.selectAudioTrack(track) },
                onDismiss = { activeSheet = ActiveSheet.NONE },
            )
            ActiveSheet.SUBTITLE -> SubtitleTrackSheet(
                tracks = state.playback.subtitleTracks,
                subtitlesEnabled = state.playback.subtitlesEnabled,
                onSelect = { track: SubtitleTrackInfo? -> viewModel.selectSubtitleTrack(track) },
                onOpenStyleSettings = { activeSheet = ActiveSheet.SUBTITLE_STYLE },
                onLoadExternal = { subtitlePickerLauncher.launch(arrayOf("*/*")) },
                onDismiss = { activeSheet = ActiveSheet.NONE },
            )
            ActiveSheet.SUBTITLE_STYLE -> SubtitleStyleSheet(
                scale = state.appSettings.subtitleScale,
                delayMs = state.appSettings.subtitleDelayMs,
                backgroundEnabled = state.appSettings.subtitleBackgroundEnabled,
                onScaleChange = { value -> coroutineScope.launch { app.settingsDataStore.setSubtitleScale(value) } },
                onDelayChange = { value -> coroutineScope.launch { app.settingsDataStore.setSubtitleDelayMs(value) } },
                onBackgroundToggle = { value -> coroutineScope.launch { app.settingsDataStore.setSubtitleBackgroundEnabled(value) } },
                onDismiss = { activeSheet = ActiveSheet.NONE },
            )
            ActiveSheet.ASPECT_RATIO -> AspectRatioSheet(
                current = state.aspectRatioMode,
                onSelect = { mode: AspectRatioMode -> viewModel.setAspectRatio(mode) },
                onDismiss = { activeSheet = ActiveSheet.NONE },
            )
            ActiveSheet.NONE -> Unit
        }
    }
}

private fun Activity.enterNexoraPip() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        enterPictureInPictureMode(params)
    }
}
