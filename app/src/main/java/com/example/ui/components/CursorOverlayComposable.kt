package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CursorStyle
import com.example.model.GestureType
import com.example.model.TrackingState
import com.example.model.UserProfile

/**
 * High-visibility Compose cursor overlay that renders the customized pointer,
 * dynamic dwell countdown ring, and gesture indicators.
 */
@Composable
fun CursorOverlayComposable(
    state: TrackingState,
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    if (!state.isTrackingActive || !state.handDetected) return

    val animatedProgress by animateFloatAsState(
        targetValue = state.dwellProgress,
        animationSpec = tween(durationMillis = 35),
        label = "dwellProgress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("cursor_overlay_box")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cursorX = state.normalizedX * size.width
            val cursorY = state.normalizedY * size.height
            val cursorColor = Color(profile.cursorColorHex)
            val cursorRadius = (profile.cursorSizeDp.dp.toPx() / 2f)

            // 1. Halo aura for high contrast visibility against any background
            drawCircle(
                color = cursorColor.copy(alpha = 0.22f),
                radius = cursorRadius * 1.5f,
                center = Offset(cursorX, cursorY)
            )

            // 2. Render chosen cursor style
            when (profile.cursorStyle) {
                CursorStyle.CYBER_RETICLE -> drawCyberReticle(cursorX, cursorY, cursorRadius, cursorColor)
                CursorStyle.NEON_ARROW -> drawNeonArrow(cursorX, cursorY, cursorRadius, cursorColor)
                CursorStyle.HIGH_VIS_CIRCLE -> drawHighVisCircle(cursorX, cursorY, cursorRadius, cursorColor)
                CursorStyle.TARGET_DOT -> drawTargetDot(cursorX, cursorY, cursorRadius, cursorColor)
            }

            // 3. Dwell Progress Ring (Clockwise circular fill)
            if (profile.showDwellRing && animatedProgress > 0f) {
                val ringRadius = cursorRadius * 1.35f
                val strokeWidth = 6.dp.toPx()

                // Background track
                drawArc(
                    color = Color.White.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(cursorX - ringRadius, cursorY - ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = strokeWidth)
                )

                // Active progress arc
                val ringColor = if (animatedProgress >= 0.95f) Color(0xFF10B981) else Color(0xFFFFD600)
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(cursorX - ringRadius, cursorY - ringRadius),
                    size = Size(ringRadius * 2, ringRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 4. Pinch / Drag Indicator
            if (state.currentGesture == GestureType.PINCH || state.isDragging) {
                drawCircle(
                    color = Color(0xFF10B981),
                    radius = cursorRadius * 0.45f,
                    center = Offset(cursorX, cursorY)
                )
            }
        }
    }
}

private fun DrawScope.drawCyberReticle(cx: Float, cy: Float, radius: Float, color: Color) {
    // Outer circle
    drawCircle(
        color = color,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 3.dp.toPx())
    )
    // Center bright dot
    drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = Offset(cx, cy)
    )
    // Crosshair ticks
    val tickLen = radius * 0.35f
    val stroke = 2.5.dp.toPx()
    drawLine(color, Offset(cx - radius - tickLen, cy), Offset(cx - radius + 3.dp.toPx(), cy), strokeWidth = stroke)
    drawLine(color, Offset(cx + radius - 3.dp.toPx(), cy), Offset(cx + radius + tickLen, cy), strokeWidth = stroke)
    drawLine(color, Offset(cx, cy - radius - tickLen), Offset(cx, cy - radius + 3.dp.toPx()), strokeWidth = stroke)
    drawLine(color, Offset(cx, cy + radius - 3.dp.toPx()), Offset(cx, cy + radius + tickLen), strokeWidth = stroke)
}

private fun DrawScope.drawNeonArrow(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy)
        lineTo(cx + radius * 1.5f, cy + radius * 0.9f)
        lineTo(cx + radius * 0.8f, cy + radius * 1.0f)
        lineTo(cx + radius * 1.2f, cy + radius * 1.8f)
        lineTo(cx + radius * 0.85f, cy + radius * 2.0f)
        lineTo(cx + radius * 0.45f, cy + radius * 1.2f)
        lineTo(cx, cy + radius * 1.5f)
        close()
    }
    drawPath(path, color)
    drawPath(path, Color.White, style = Stroke(width = 2.dp.toPx()))
}

private fun DrawScope.drawHighVisCircle(cx: Float, cy: Float, radius: Float, color: Color) {
    // Outer bright halo
    drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = radius + 3.dp.toPx(), center = Offset(cx, cy), style = Stroke(width = 4.dp.toPx()))
    drawCircle(color = color, radius = radius, center = Offset(cx, cy), style = Stroke(width = 5.dp.toPx()))
    drawCircle(color = Color.White, radius = radius * 0.4f, center = Offset(cx, cy))
}

private fun DrawScope.drawTargetDot(cx: Float, cy: Float, radius: Float, color: Color) {
    drawCircle(color = color.copy(alpha = 0.4f), radius = radius, center = Offset(cx, cy))
    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(cx, cy))
    drawCircle(color = color, radius = 2.5.dp.toPx(), center = Offset(cx, cy))
}
