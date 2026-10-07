package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Android Accessibility Service that executes synthetic touch gestures (clicks, swipes, scrolls)
 * and performs system-wide navigation actions on behalf of mobility-impaired users.
 */
class AuraAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)

    companion object {
        var instance: AuraAccessibilityService? = null
            private set

        val isRunning: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used for active gesture injection
    }

    override fun onInterrupt() {
        // Required callback
    }

    /**
     * Synthetically taps the screen at pixel coordinates (x, y).
     */
    fun clickAt(x: Float, y: Float, durationMs: Long = 50L) {
        val clickPath = Path().apply {
            moveTo(x.coerceAtLeast(0f), y.coerceAtLeast(0f))
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(clickPath, 0L, durationMs))
            .build()

        dispatchGesture(gesture, null, null)
    }

    /**
     * Synthetically double taps at (x, y).
     */
    fun doubleClickAt(x: Float, y: Float) {
        clickAt(x, y, 40L)
        serviceScope.launch {
            delay(120L)
            clickAt(x, y, 40L)
        }
    }

    /**
     * Synthetically long presses at (x, y).
     */
    fun longPressAt(x: Float, y: Float) {
        clickAt(x, y, 650L)
    }

    /**
     * Synthetically swipes/drags from (startX, startY) to (endX, endY).
     */
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 350L) {
        val swipePath = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0L, durationMs))
            .build()

        dispatchGesture(gesture, null, null)
    }

    /**
     * Scrolls the current screen up or down.
     */
    fun scroll(up: Boolean) {
        val dm = resources.displayMetrics
        val centerX = (dm.widthPixels / 2).toFloat()
        val startY = if (up) (dm.heightPixels * 0.35f) else (dm.heightPixels * 0.75f)
        val endY = if (up) (dm.heightPixels * 0.75f) else (dm.heightPixels * 0.35f)
        swipe(centerX, startY, centerX, endY, 300L)
    }

    // Global navigation actions
    fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun performNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    fun performQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    fun performLockScreen(): Boolean = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
}
