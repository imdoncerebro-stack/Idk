package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Operating mode for translating hand movement to cursor coordinates.
 */
enum class TrackingMode(val displayName: String, val description: String) {
    ABSOLUTE(
        "Absolute Direct",
        "Directly maps hand position within camera frame to phone screen coordinates."
    ),
    RELATIVE_JOYSTICK(
        "Relative Joystick",
        "Displacing hand from resting center moves cursor with velocity proportional to distance. Ideal for limited range of motion."
    ),
    AIR_MOUSE(
        "Differential Air Mouse",
        "Tracks delta hand movements like a physical air mouse with acceleration curves."
    )
}

/**
 * Detected hand pose or gesture.
 */
enum class GestureType(val label: String, val iconName: String) {
    NONE("Idle / Searching", "Search"),
    OPEN_PALM("Open Palm", "PanTool"),
    POINTING("Pointing Finger", "TouchApp"),
    PINCH("Pinch (Thumb + Index)", "Pinch"),
    FIST("Fist / Grasp", "FrontHand"),
    SWIPE_LEFT("Swipe Left", "ArrowBack"),
    SWIPE_RIGHT("Swipe Right", "ArrowForward"),
    SWIPE_UP("Swipe Up", "ArrowUpward"),
    SWIPE_DOWN("Swipe Down", "ArrowDownward"),
    DWELL("Dwell Hovering", "Timer")
}

/**
 * Action triggered by dwell hover or gesture.
 */
enum class DwellAction(val title: String, val icon: String) {
    TAP("Single Tap", "touch_app"),
    DOUBLE_TAP("Double Tap", "touch_app_double"),
    LONG_PRESS("Long Press", "touch_app_hold"),
    DRAG_TOGGLE("Drag & Drop", "drag_indicator"),
    SCROLL_UP("Scroll Up", "arrow_upward"),
    SCROLL_DOWN("Scroll Down", "arrow_downward"),
    HOME("System Home", "home"),
    BACK("System Back", "arrow_back"),
    RECENTS("Recent Apps", "apps")
}

/**
 * Visual styling options for the on-screen cursor.
 */
enum class CursorStyle(val displayName: String) {
    CYBER_RETICLE("Cyber Reticle"),
    NEON_ARROW("Neon Pointer Arrow"),
    HIGH_VIS_CIRCLE("High-Visibility Halo"),
    TARGET_DOT("Precision Crosshair Dot")
}

/**
 * User configuration and accessibility profile.
 */
data class UserProfile(
    val id: String = "default",
    val presetName: String = "Balanced Standard",
    val sensitivityX: Float = 1.3f,
    val sensitivityY: Float = 1.3f,
    val smoothingFactor: Float = 0.22f, // Lower = heavier smoothing (tremor filter)
    val deadZoneRadius: Float = 0.025f,
    val dwellTimeMs: Long = 900L,
    val dwellToleranceRadius: Float = 0.045f,
    val cursorSizeDp: Int = 44,
    val cursorStyle: CursorStyle = CursorStyle.CYBER_RETICLE,
    val cursorColorHex: Long = 0xFF00E5FF, // Cyber Cyan
    val showDwellRing: Boolean = true,
    val showCursorTrail: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val audioFeedbackEnabled: Boolean = true,
    val pinchClickEnabled: Boolean = true,
    val hotEdgesEnabled: Boolean = true,
    val trackingMode: TrackingMode = TrackingMode.ABSOLUTE,
    val defaultDwellAction: DwellAction = DwellAction.TAP
) {
    companion object {
        val BALANCED = UserProfile(
            presetName = "Balanced Standard",
            sensitivityX = 1.3f,
            sensitivityY = 1.3f,
            smoothingFactor = 0.22f,
            deadZoneRadius = 0.025f,
            dwellTimeMs = 900L,
            cursorSizeDp = 44,
            cursorColorHex = 0xFF00E5FF
        )

        val TREMOR_RELIEF = UserProfile(
            presetName = "Tremor Relief",
            sensitivityX = 1.0f,
            sensitivityY = 1.0f,
            smoothingFactor = 0.10f, // Heavy smoothing
            deadZoneRadius = 0.055f, // Large deadzone
            dwellTimeMs = 1200L, // Extra dwell stability
            dwellToleranceRadius = 0.070f,
            cursorSizeDp = 52,
            cursorColorHex = 0xFFFFD600 // High visibility amber
        )

        val LIMITED_RANGE_OF_MOTION = UserProfile(
            presetName = "Low Range / Fatigue Reduction",
            sensitivityX = 2.4f,
            sensitivityY = 2.4f,
            smoothingFactor = 0.28f,
            deadZoneRadius = 0.015f,
            dwellTimeMs = 700L,
            trackingMode = TrackingMode.RELATIVE_JOYSTICK,
            cursorSizeDp = 48,
            cursorColorHex = 0xFFB388FF // Neon violet
        )

        val PRECISION_CLICKER = UserProfile(
            presetName = "High Precision",
            sensitivityX = 0.9f,
            sensitivityY = 0.9f,
            smoothingFactor = 0.18f,
            deadZoneRadius = 0.020f,
            dwellTimeMs = 850L,
            cursorStyle = CursorStyle.TARGET_DOT,
            cursorSizeDp = 36,
            cursorColorHex = 0xFF00E5FF
        )

        val SPEED_NAVIGATOR = UserProfile(
            presetName = "Speed Navigator",
            sensitivityX = 1.8f,
            sensitivityY = 1.8f,
            smoothingFactor = 0.35f,
            deadZoneRadius = 0.020f,
            dwellTimeMs = 600L,
            cursorSizeDp = 40,
            cursorColorHex = 0xFF00E676 // Bright emerald
        )
    }
}

/**
 * Real-time state of the hand tracking and cursor system.
 */
data class TrackingState(
    val isTrackingActive: Boolean = false,
    val isCameraStreaming: Boolean = false,
    val handDetected: Boolean = false,
    val normalizedX: Float = 0.5f,
    val normalizedY: Float = 0.5f,
    val rawX: Float = 0.5f,
    val rawY: Float = 0.5f,
    val screenX: Float = 540f,
    val screenY: Float = 1170f,
    val dwellProgress: Float = 0f,
    val isDwellTriggered: Boolean = false,
    val currentGesture: GestureType = GestureType.NONE,
    val activeDwellAction: DwellAction = DwellAction.TAP,
    val isDragging: Boolean = false,
    val fps: Int = 0,
    val latencyMs: Long = 0L,
    val confidence: Float = 0f,
    val statusMessage: String = "Ready to start",
    val lastActionNotification: String? = null,
    val isSimulatorMode: Boolean = false
)
