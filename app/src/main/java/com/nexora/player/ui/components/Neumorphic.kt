package com.nexora.player.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexora.player.ui.theme.nexoraColors

/**
 * The single reusable depth treatment every raised surface in the app uses: a
 * dark, blurred ambient drop-shadow beneath a frosted-glass panel, with a thin
 * glass-edge rim around it — the "glow" theme's answer to a Material elevation
 * shadow. Ported from the reference dark dashboard-card design (see
 * [com.nexora.player.ui.theme.NexoraExtendedColors.shadowDark]/[glassBorder]).
 *
 * [pressed] swaps the plain ambient shadow and rim for a colored neon glow in
 * [com.nexora.player.ui.theme.NexoraExtendedColors.accentGlow]/[accent] — the
 * look used for the currently active/selected control (e.g. the selected
 * bottom-nav icon), echoing the glowing accent ring in the reference design.
 *
 * Must come *before* `.background(...)` in the modifier chain: this draws only
 * the shadow and rim (the shape itself stays transparent here), so the
 * background drawn on top hides the shadow directly under the shape and only
 * its blurred edges/rim peek out.
 */
fun Modifier.neumorphic(
    cornerRadius: Dp,
    elevation: Dp = 8.dp,
    pressed: Boolean = false,
    lightShadowColor: Color? = null,
    darkShadowColor: Color? = null,
): Modifier = composed {
    val colors = MaterialTheme.nexoraColors
    val rimColor = lightShadowColor ?: (if (pressed) colors.accent.copy(alpha = 0.9f) else colors.glassBorder)
    val shadowColor = darkShadowColor ?: (if (pressed) colors.accentGlow else colors.shadowDark)

    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawBehind {
            val elevationPx = elevation.toPx()
            val radiusPx = cornerRadius.toPx().coerceAtMost(kotlin.math.min(size.width, size.height) / 2f)
            val androidPath = android.graphics.Path().apply {
                addRoundRect(0f, 0f, size.width, size.height, radiusPx, radiusPx, android.graphics.Path.Direction.CW)
            }

            // alpha = 1 (not 0): some Android versions skip the shadow layer entirely
            // when the fill paint is fully transparent, so the shape stays invisible
            // (alpha 1/255) while guaranteeing its shadow still renders.
            val invisibleFill = android.graphics.Color.argb(1, 0, 0, 0)
            val blurRadius = if (pressed) elevationPx * 2.4f else elevationPx * 1.5f
            val dy = if (pressed) 0f else elevationPx * 0.6f

            drawIntoCanvas { canvas ->
                val shadowPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = invisibleFill
                    setShadowLayer(blurRadius, 0f, dy, shadowColor.toArgb())
                }
                canvas.nativeCanvas.drawPath(androidPath, shadowPaint)
            }

            // Thin glass-edge rim — a bright accent ring when this surface is the
            // active/selected one, a subtle frosted border otherwise.
            drawRoundRect(
                color = rimColor,
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = (if (pressed) 1.6f else 1f).dp.toPx()),
            )
        }
}
