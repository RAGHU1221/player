package com.nexora.player

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.media3.common.util.UnstableApi
import com.nexora.player.data.datastore.AppSettings
import com.nexora.player.ui.navigation.NexoraNavHost
import com.nexora.player.ui.theme.NexoraTheme
import com.nexora.player.ui.theme.toExtendedColors

@UnstableApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = application as NexoraApplication
            val settings by app.settingsDataStore.settingsFlow.collectAsState(initial = AppSettings())

            // Neumorphic Light needs dark status/nav bar icons to stay visible on its
            // pale background; Neumorphic Dark keeps the light icons it always had.
            val isLightTheme = settings.themeVariant.toExtendedColors().isLight
            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = isLightTheme
                insetsController.isAppearanceLightNavigationBars = isLightTheme
            }

            NexoraTheme(variant = settings.themeVariant) {
                NexoraNavHost()
            }
        }
    }

    /** Home button pressed while a video is playing → auto-enter PiP (section 16). */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val controller = (application as NexoraApplication).playerController ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && controller.player.isPlaying) {
            val params = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
            runCatching { enterPictureInPictureMode(params) }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        // Compose content keeps rendering the same PlayerSurface; our own control
        // overlay auto-hides after a few seconds anyway (see PlayerScreen), which
        // is sufficient for a clean PiP window without extra state plumbing.
    }
}
