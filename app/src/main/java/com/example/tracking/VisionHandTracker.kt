package com.example.tracking

import androidx.camera.core.ImageProxy
import com.example.model.GestureType
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Result of vision hand analysis for a single frame.
 */
data class HandAnalysisResult(
    val handDetected: Boolean,
    val normalizedX: Float,
    val normalizedY: Float,
    val gestureType: GestureType,
    val confidence: Float,
    val palmCenterX: Float,
    val palmCenterY: Float,
    val fingertipX: Float,
    val fingertipY: Float,
    val boundingBox: HandBoundingBox? = null
)

data class HandBoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Real-time on-device vision hand tracker processing CameraX YUV_420_888 frames.
 * Fast, lightweight, and battery-friendly.
 */
class VisionHandTracker {

    private var prevFingertipY: Float = 0.5f
    private var prevFingertipX: Float = 0.5f
    private var prevArea: Float = 0f
    private var pinchHoldFrames: Int = 0

    // Calibrated resting center
    var restingCenterX: Float = 0.5f
    var restingCenterY: Float = 0.5f

    /**
     * Analyzes a CameraX ImageProxy frame.
     */
    fun processImage(image: ImageProxy, isFrontCamera: Boolean = true): HandAnalysisResult {
        try {
            val planes = image.planes
            if (planes.size < 3) {
                return emptyResult()
            }

            val yPlane = planes[0]
            val uPlane = planes[1]
            val vPlane = planes[2]

            val yBuffer: ByteBuffer = yPlane.buffer
            val uBuffer: ByteBuffer = uPlane.buffer
            val vBuffer: ByteBuffer = vPlane.buffer

            val width = image.width
            val height = image.height
            val yRowStride = yPlane.rowStride
            val uvRowStride = uPlane.rowStride
            val uvPixelStride = uPlane.pixelStride

            // Grid sub-sampling for real-time 30-60 FPS performance
            val stepX = max(4, width / 64)
            val stepY = max(4, height / 64)

            var count = 0
            var sumX = 0f
            var sumY = 0f

            var minX = 1f
            var maxX = 0f
            var minY = 1f
            var maxY = 0f

            // Topmost extremity point (pointing finger)
            var topY = 1.0f
            var topX = 0.5f

            var y = 0
            while (y < height) {
                val yRowOffset = y * yRowStride
                val uvRowOffset = (y / 2) * uvRowStride

                var x = 0
                while (x < width) {
                    val yIndex = yRowOffset + x
                    val uvIndex = uvRowOffset + (x / 2) * uvPixelStride

                    if (yIndex < yBuffer.limit() && uvIndex < uBuffer.limit() && uvIndex < vBuffer.limit()) {
                        val yVal = yBuffer.get(yIndex).toInt() and 0xFF
                        val uVal = uBuffer.get(uvIndex).toInt() and 0xFF
                        val vVal = vBuffer.get(uvIndex).toInt() and 0xFF

                        // YCbCr skin tone detection:
                        // Cb (uVal) in [77, 130], Cr (vVal) in [132, 178], Y > 40
                        if (yVal in 40..245 && uVal in 75..132 && vVal in 130..180) {
                            val normX = x.toFloat() / width.toFloat()
                            val normY = y.toFloat() / height.toFloat()

                            sumX += normX
                            sumY += normY
                            count++

                            if (normX < minX) minX = normX
                            if (normX > maxX) maxX = normX
                            if (normY < minY) minY = normY
                            if (normY > maxY) maxY = normY

                            // Check topmost pixel for pointing fingertip
                            if (normY < topY) {
                                topY = normY
                                topX = normX
                            }
                        }
                    }
                    x += stepX
                }
                y += stepY
            }

            // Minimum skin pixels threshold
            val minSkinPixels = 25
            if (count < minSkinPixels) {
                return emptyResult()
            }

            val rawPalmX = sumX / count
            val rawPalmY = sumY / count

            // Front camera mirroring: flip X so moving right on camera moves cursor right
            val palmX = if (isFrontCamera) (1f - rawPalmX) else rawPalmX
            val palmY = rawPalmY

            val fingerX = if (isFrontCamera) (1f - topX) else topX
            val fingerY = topY

            val handWidth = maxX - minX
            val handHeight = maxY - minY
            val currentArea = handWidth * handHeight
            val aspectRatio = if (handWidth > 0f) handHeight / handWidth else 1f

            // Gesture classification
            val gesture = classifyGesture(
                aspectRatio = aspectRatio,
                handHeight = handHeight,
                currentArea = currentArea,
                palmY = palmY,
                fingerY = fingerY,
                fingerX = fingerX
            )

            // Primary tracking point: if pointing, follow fingertip; otherwise follow palm centroid
            val trackX = if (gesture == GestureType.POINTING || gesture == GestureType.PINCH) fingerX else palmX
            val trackY = if (gesture == GestureType.POINTING || gesture == GestureType.PINCH) fingerY else palmY

            prevFingertipX = fingerX
            prevFingertipY = fingerY
            prevArea = currentArea

            val confidence = (count.toFloat() / 250f).coerceIn(0.4f, 1f)

            val box = if (isFrontCamera) {
                HandBoundingBox(1f - maxX, minY, 1f - minX, maxY)
            } else {
                HandBoundingBox(minX, minY, maxX, maxY)
            }

            return HandAnalysisResult(
                handDetected = true,
                normalizedX = trackX.coerceIn(0f, 1f),
                normalizedY = trackY.coerceIn(0f, 1f),
                gestureType = gesture,
                confidence = confidence,
                palmCenterX = palmX,
                palmCenterY = palmY,
                fingertipX = fingerX,
                fingertipY = fingerY,
                boundingBox = box
            )
        } catch (e: Exception) {
            return emptyResult()
        }
    }

    private fun classifyGesture(
        aspectRatio: Float,
        handHeight: Float,
        currentArea: Float,
        palmY: Float,
        fingerY: Float,
        fingerX: Float
    ): GestureType {
        val fingerProtrusion = palmY - fingerY

        // Pinch check: area contraction or rapid fingertip closure
        val areaDelta = if (prevArea > 0f) (prevArea - currentArea) / prevArea else 0f
        if (areaDelta > 0.18f && fingerProtrusion < 0.15f) {
            pinchHoldFrames++
            if (pinchHoldFrames in 1..8) {
                return GestureType.PINCH
            }
        } else {
            pinchHoldFrames = 0
        }

        // Fist check: compact aspect ratio with low protrusion
        if (aspectRatio in 0.8f..1.3f && fingerProtrusion < 0.08f && currentArea < 0.20f) {
            return GestureType.FIST
        }

        // Pointing check: high vertical protrusion of fingertip above palm
        if (fingerProtrusion > 0.12f && aspectRatio > 1.1f) {
            return GestureType.POINTING
        }

        // Open Palm: broad area and moderate protrusion
        if (currentArea > 0.12f && aspectRatio in 0.9f..1.8f) {
            return GestureType.OPEN_PALM
        }

        return GestureType.POINTING
    }

    private fun emptyResult() = HandAnalysisResult(
        handDetected = false,
        normalizedX = 0.5f,
        normalizedY = 0.5f,
        gestureType = GestureType.NONE,
        confidence = 0f,
        palmCenterX = 0.5f,
        palmCenterY = 0.5f,
        fingertipX = 0.5f,
        fingertipY = 0.5f
    )
}
