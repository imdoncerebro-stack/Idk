package com.example.tracking

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * High-performance, tremor-filtering motion smoother for accessibility hand tracking.
 * Combines Adaptive Double Exponential Smoothing, velocity-based dynamic damping,
 * and calibrated dead-zones.
 */
class MotionFilter(
    private var baseSmoothing: Float = 0.22f,
    private var deadZoneRadius: Float = 0.025f,
    private var sensitivityX: Float = 1.3f,
    private var sensitivityY: Float = 1.3f
) {
    private var filteredX: Float = 0.5f
    private var filteredY: Float = 0.5f

    private var trendX: Float = 0f
    private var trendY: Float = 0f

    private var lastRawX: Float = 0.5f
    private var lastRawY: Float = 0.5f

    private var isInitialized = false

    fun updateConfig(
        baseSmoothing: Float,
        deadZoneRadius: Float,
        sensitivityX: Float,
        sensitivityY: Float
    ) {
        this.baseSmoothing = baseSmoothing.coerceIn(0.04f, 0.8f)
        this.deadZoneRadius = deadZoneRadius.coerceIn(0.005f, 0.15f)
        this.sensitivityX = sensitivityX.coerceIn(0.2f, 4.0f)
        this.sensitivityY = sensitivityY.coerceIn(0.2f, 4.0f)
    }

    fun reset(initialX: Float = 0.5f, initialY: Float = 0.5f) {
        filteredX = initialX
        filteredY = initialY
        trendX = 0f
        trendY = 0f
        lastRawX = initialX
        lastRawY = initialY
        isInitialized = true
    }

    /**
     * Filters new raw normalized input coordinates (0f..1f).
     * Returns smoothed normalized coordinates (0f..1f).
     */
    fun filter(rawX: Float, rawY: Float): Pair<Float, Float> {
        if (!isInitialized) {
            reset(rawX, rawY)
            return Pair(filteredX, filteredY)
        }

        val deltaX = rawX - lastRawX
        val deltaY = rawY - lastRawY
        val rawDist = hypot(deltaX, deltaY)

        // 1. Deadzone check for resting tremor reduction
        if (rawDist < deadZoneRadius) {
            // Motion is within tremor jitter window; heavily damp
            lastRawX = rawX
            lastRawY = rawY
            return Pair(filteredX, filteredY)
        }

        // 2. Velocity-based adaptive alpha
        // Higher velocity = higher alpha (more responsive, less lag during fast reach)
        // Lower velocity = lower alpha (smoother, eliminating fine shakes when aiming)
        val velocityFactor = (rawDist / 0.15f).coerceIn(0f, 1f)
        val dynamicAlpha = (baseSmoothing + (0.85f - baseSmoothing) * velocityFactor.pow(1.5f))
            .coerceIn(0.04f, 0.95f)

        // 3. Apply sensitivity around the center
        val centerOffsetX = (rawX - 0.5f) * sensitivityX
        val centerOffsetY = (rawY - 0.5f) * sensitivityY
        val adjustedTargetX = (0.5f + centerOffsetX).coerceIn(0f, 1f)
        val adjustedTargetY = (0.5f + centerOffsetY).coerceIn(0f, 1f)

        // 4. Double Exponential Smoothing
        val prevFilteredX = filteredX
        val prevFilteredY = filteredY

        filteredX = dynamicAlpha * adjustedTargetX + (1f - dynamicAlpha) * (prevFilteredX + trendX)
        filteredY = dynamicAlpha * adjustedTargetY + (1f - dynamicAlpha) * (prevFilteredY + trendY)

        val beta = 0.1f
        trendX = beta * (filteredX - prevFilteredX) + (1f - beta) * trendX
        trendY = beta * (filteredY - prevFilteredY) + (1f - beta) * trendY

        filteredX = filteredX.coerceIn(0f, 1f)
        filteredY = filteredY.coerceIn(0f, 1f)

        lastRawX = rawX
        lastRawY = rawY

        return Pair(filteredX, filteredY)
    }

    fun getFilteredPosition(): Pair<Float, Float> = Pair(filteredX, filteredY)
}
