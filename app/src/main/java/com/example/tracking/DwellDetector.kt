package com.example.tracking

import kotlin.math.hypot

/**
 * Detects intentional hovering (dwelling) over a screen target to execute hands-free clicks.
 */
class DwellDetector(
    private var dwellDurationMs: Long = 900L,
    private var toleranceRadius: Float = 0.045f,
    private var cooldownMs: Long = 500L
) {
    private var anchorX: Float = 0.5f
    private var anchorY: Float = 0.5f
    private var dwellStartTime: Long = 0L
    private var lastTriggerTime: Long = 0L
    private var isDwelling: Boolean = false
    private var isTriggered: Boolean = false

    fun updateConfig(dwellDurationMs: Long, toleranceRadius: Float) {
        this.dwellDurationMs = dwellDurationMs.coerceIn(300L, 3000L)
        this.toleranceRadius = toleranceRadius.coerceIn(0.015f, 0.20f)
    }

    fun reset() {
        isDwelling = false
        isTriggered = false
        dwellStartTime = 0L
    }

    /**
     * Updates dwell state with current normalized coordinates (0f..1f).
     * Returns a Pair of:
     * - dwellProgress: Float from 0f to 1f
     * - triggered: Boolean (true on the exact frame the dwell completed)
     */
    fun update(currentX: Float, currentY: Float, currentTimeMs: Long = System.currentTimeMillis()): Pair<Float, Boolean> {
        // Enforce cooldown after a successful click
        if (currentTimeMs - lastTriggerTime < cooldownMs) {
            return Pair(0f, false)
        }

        if (!isDwelling) {
            // Start new dwell anchor
            anchorX = currentX
            anchorY = currentY
            dwellStartTime = currentTimeMs
            isDwelling = true
            isTriggered = false
            return Pair(0f, false)
        }

        val distance = hypot(currentX - anchorX, currentY - anchorY)

        if (distance > toleranceRadius) {
            // Moved outside tolerance ring: reset dwell anchor to new position
            anchorX = currentX
            anchorY = currentY
            dwellStartTime = currentTimeMs
            isTriggered = false
            return Pair(0f, false)
        }

        // Inside tolerance ring: calculate elapsed progress
        val elapsed = currentTimeMs - dwellStartTime
        val progress = (elapsed.toFloat() / dwellDurationMs.toFloat()).coerceIn(0f, 1f)

        if (progress >= 1f && !isTriggered) {
            isTriggered = true
            lastTriggerTime = currentTimeMs
            // Reset dwelling to start new cycle after cooldown
            isDwelling = false
            return Pair(1f, true)
        }

        return Pair(progress, false)
    }

    fun getAnchorPosition(): Pair<Float, Float> = Pair(anchorX, anchorY)
}
