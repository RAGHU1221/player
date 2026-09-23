package com.nexora.player.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexora.player.ui.theme.nexoraColors

/**
 * The soft-UI ("neumorphic") shadow pair every raised surface in the app uses: a
 * light highlight offset toward the top-left and a dark umbra offset toward the
 * bottom-right, both blurred — the classic embossed look ported from the
 * reference Neumorphism theme's dual `box-shadow`. Defaults to the current
 * theme's [com.nexora.player.ui.theme.NexoraExtendedColors.shadowLight]/
 * [com.nexora.player.ui.theme.NexoraExtendedColors.shadowDark]; pass explicit
 * colors to override.
 *
 * [pressed] flips the two shadows inward, simulating a control being pushed in
 * (a selected nav item, a pressed button).
 *
 * Must come *before* `.background(...)` in the modifier chain: this draws only
 * the shadows (the shape itself stays transparent here), so the background drawn
 * on top hides the shadow directly under the shape and only its blurred edges
 * peek out — exactly like the CSS technique this is ported from.
 */
fun Modifier.neumorphic(
    cornerRadius: Dp,
    elevation: Dp = 8.dp,
    pressed: Boolean = false,
    lightShadowColor: Color? = null,
    darkShadowColor: Color? = null,
): Modifier = composed {
    val colors = MaterialTheme.nexoraColors
    val light = lightShadowColor ?: colors.shadowLight
    val dark = darkShadowColor ?: colors.shadowDark

    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawBehind {
            val elevationPx = elevation.toPx()
            val radiusPx = cornerRadius.toPx().coerceAtMost(kotlin.math.min(size.width, size.height) / 2f)
            val androidPath = android.graphics.Path().apply {
                addRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    radiusPx,
                    radiusPx,
                    android.graphics.Path.Direction.CW,
                )
            }
            val sign = if (pressed) -1f else 1f

            // alpha = 1 (not 0): some Android versions skip the shadow layer entirely
            // when the fill paint is fully transparent, so the shape stays invisible
            // (alpha 1/255) while guaranteeing its shadow still renders.
            val invisibleFill = android.graphics.Color.argb(1, 0, 0, 0)

            drawIntoCanvas { canvas ->
                val darkPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = invisibleFill
                    setShadowLayer(elevationPx, elevationPx * sign, elevationPx * sign, dark.toArgb())
                }
                canvas.nativeCanvas.drawPath(androidPath, darkPaint)

                val lightPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = invisibleFill
                    setShadowLayer(elevationPx, -elevationPx * sign, -elevationPx * sign, light.toArgb())
                }
                canvas.nativeCanvas.drawPath(androidPath, lightPaint)
            }
        }
}
