package com.example.tracking

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.core.ImageProxy
import com.example.model.CursorStyle
import com.example.model.DwellAction
import com.example.model.GestureType
import com.example.model.TrackingMode
import com.example.model.TrackingState
import com.example.model.UserProfile
import com.example.service.AuraAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Central singleton orchestrator coordinating camera vision, motion smoothing,
 * dwell clicking, accessibility injection, and user profiles.
 */
object TrackingController {

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile.BALANCED)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val motionFilter = MotionFilter()
    private val dwellDetector = DwellDetector()
    val visionHandTracker = VisionHandTracker()

    private var appContext: Context? = null
    private var screenWidthPx: Int = 1080
    private var screenHeightPx: Int = 2340

    // Frame metrics
    private var frameCount = 0
    private var lastFpsTimestamp = System.currentTimeMillis()
    private var currentFps = 0

    // Virtual simulator simulation job
    private var simulatorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val dm = context.resources.displayMetrics
        screenWidthPx = dm.widthPixels
        screenHeightPx = dm.heightPixels

        updateConfigFromProfile(_userProfile.value)
    }

    fun updateScreenDimensions(width: Int, height: Int) {
        if (width > 0 && height > 0) {
            screenWidthPx = width
            screenHeightPx = height
        }
    }

    fun updateProfile(profile: UserProfile) {
        _userProfile.value = profile
        updateConfigFromProfile(profile)
    }

    fun selectPreset(preset: UserProfile) {
        _userProfile.value = preset
        updateConfigFromProfile(preset)
        notifyAction("Profile: ${preset.presetName}")
    }

    private fun updateConfigFromProfile(profile: UserProfile) {
        motionFilter.updateConfig(
            baseSmoothing = profile.smoothingFactor,
            deadZoneRadius = profile.deadZoneRadius,
            sensitivityX = profile.sensitivityX,
            sensitivityY = profile.sensitivityY
        )
        dwellDetector.updateConfig(
            dwellDurationMs = profile.dwellTimeMs,
            toleranceRadius = profile.dwellToleranceRadius
        )
    }

    fun startTracking() {
        motionFilter.reset()
        dwellDetector.reset()
        _trackingState.update {
            it.copy(
                isTrackingActive = true,
                statusMessage = "Tracking active. Move your hand to guide cursor."
            )
        }
    }

    fun stopTracking() {
        stopSimulator()
        _trackingState.update {
            it.copy(
                isTrackingActive = false,
                isCameraStreaming = false,
                handDetected = false,
                dwellProgress = 0f,
                statusMessage = "Tracking paused"
            )
        }
    }

    fun toggleTracking(): Boolean {
        if (_trackingState.value.isTrackingActive) {
            stopTracking()
            return false
        } else {
            startTracking()
            return true
        }
    }

    /**
     * Process an incoming camera frame.
     */
    fun onCameraFrame(image: ImageProxy, isFrontCamera: Boolean = true) {
        val startTime = System.currentTimeMillis()
        val analysis = visionHandTracker.processImage(image, isFrontCamera)
        val latency = System.currentTimeMillis() - startTime

        // Compute FPS
        frameCount++
        val now = System.currentTimeMillis()
        if (now - lastFpsTimestamp >= 1000L) {
            currentFps = frameCount
            frameCount = 0
            lastFpsTimestamp = now
        }

        if (!_trackingState.value.isTrackingActive) {
            _trackingState.update {
                it.copy(
                    isCameraStreaming = true,
                    handDetected = analysis.handDetected,
                    fps = currentFps,
                    latencyMs = latency
                )
            }
            return
        }

        if (!analysis.handDetected) {
            dwellDetector.reset()
            _trackingState.update {
                it.copy(
                    isCameraStreaming = true,
                    handDetected = false,
                    dwellProgress = 0f,
                    currentGesture = GestureType.NONE,
                    fps = currentFps,
                    latencyMs = latency,
                    statusMessage = "Searching for hand in frame..."
                )
            }
            return
        }

        // Apply motion filter
        val (filteredX, filteredY) = motionFilter.filter(analysis.normalizedX, analysis.normalizedY)
        val screenX = filteredX * screenWidthPx
        val screenY = filteredY * screenHeightPx

        // Process Dwell
        val (dwellProgress, dwellTriggered) = dwellDetector.update(filteredX, filteredY, now)

        // Process Pinch click
        val isPinch = analysis.gestureType == GestureType.PINCH && _userProfile.value.pinchClickEnabled

        _trackingState.update {
            it.copy(
                isCameraStreaming = true,
                handDetected = true,
                rawX = analysis.normalizedX,
                rawY = analysis.normalizedY,
                normalizedX = filteredX,
                normalizedY = filteredY,
                screenX = screenX,
                screenY = screenY,
                dwellProgress = dwellProgress,
                isDwellTriggered = dwellTriggered,
                currentGesture = analysis.gestureType,
                fps = currentFps,
                latencyMs = latency,
                confidence = analysis.confidence,
                statusMessage = if (isPinch) "Pinch Click!" else if (dwellTriggered) "Dwell Click!" else "Tracking hand"
            )
        }

        if (dwellTriggered || isPinch) {
            executeActionAt(screenX, screenY, _trackingState.value.activeDwellAction)
        }
    }

    /**
     * Simulator input for emulator, preview, or on-screen touch testing.
     */
    fun onSimulatorInput(normX: Float, normY: Float, gesture: GestureType = GestureType.POINTING) {
        val (filteredX, filteredY) = motionFilter.filter(normX, normY)
        val screenX = filteredX * screenWidthPx
        val screenY = filteredY * screenHeightPx

        val now = System.currentTimeMillis()
        val (dwellProgress, dwellTriggered) = dwellDetector.update(filteredX, filteredY, now)
        val isPinch = gesture == GestureType.PINCH

        _trackingState.update {
            it.copy(
                isTrackingActive = true,
                handDetected = true,
                normalizedX = filteredX,
                normalizedY = filteredY,
                rawX = normX,
                rawY = normY,
                screenX = screenX,
                screenY = screenY,
                dwellProgress = dwellProgress,
                isDwellTriggered = dwellTriggered,
                currentGesture = gesture,
                isSimulatorMode = true,
                statusMessage = if (isPinch) "Pinch Click!" else if (dwellTriggered) "Dwell Click!" else "Simulator Active"
            )
        }

        if (dwellTriggered || isPinch) {
            executeActionAt(screenX, screenY, _trackingState.value.activeDwellAction)
        }
    }

    fun startAutomatedSimulatorDemo() {
        stopSimulator()
        startTracking()
        _trackingState.update { it.copy(isSimulatorMode = true) }

        simulatorJob = scope.launch {
            var angle = 0.0
            while (_trackingState.value.isTrackingActive && _trackingState.value.isSimulatorMode) {
                angle += 0.08
                val x = 0.5f + (0.35f * kotlin.math.sin(angle)).toFloat()
                val y = 0.5f + (0.30f * kotlin.math.cos(angle * 0.7)).toFloat()

                val gesture = if ((angle.toInt() % 8) == 0) GestureType.PINCH else GestureType.POINTING
                onSimulatorInput(x, y, gesture)
                delay(33) // ~30 FPS
            }
        }
    }

    fun stopSimulator() {
        simulatorJob?.cancel()
        simulatorJob = null
        _trackingState.update { it.copy(isSimulatorMode = false) }
    }

    fun setActiveDwellAction(action: DwellAction) {
        _trackingState.update { it.copy(activeDwellAction = action) }
        notifyAction("Dwell Mode: ${action.title}")
    }

    fun recalibrateCenter() {
        val currX = _trackingState.value.rawX
        val currY = _trackingState.value.rawY
        visionHandTracker.restingCenterX = currX
        visionHandTracker.restingCenterY = currY
        motionFilter.reset(0.5f, 0.5f)
        dwellDetector.reset()
        notifyAction("Center Calibrated")
    }

    private fun executeActionAt(x: Float, y: Float, action: DwellAction) {
        vibrateFeedback()

        val service = AuraAccessibilityService.instance
        if (service != null) {
            when (action) {
                DwellAction.TAP -> service.clickAt(x, y)
                DwellAction.DOUBLE_TAP -> service.doubleClickAt(x, y)
                DwellAction.LONG_PRESS -> service.longPressAt(x, y)
                DwellAction.DRAG_TOGGLE -> {
                    val dragging = !_trackingState.value.isDragging
                    _trackingState.update { it.copy(isDragging = dragging) }
                    notifyAction(if (dragging) "Drag Started" else "Drag Dropped")
                }
                DwellAction.SCROLL_UP -> service.scroll(up = true)
                DwellAction.SCROLL_DOWN -> service.scroll(up = false)
                DwellAction.HOME -> service.performHome()
                DwellAction.BACK -> service.performBack()
                DwellAction.RECENTS -> service.performRecents()
            }
        } else {
            notifyAction("Executed: ${action.title} at (${x.toInt()}, ${y.toInt()})")
        }
    }

    private fun vibrateFeedback() {
        if (!_userProfile.value.hapticFeedbackEnabled) return
        val ctx = appContext ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v?.vibrate(45L)
                }
            }
        } catch (_: Exception) {}
    }

    fun notifyAction(message: String) {
        _trackingState.update { it.copy(lastActionNotification = message) }
        scope.launch {
            delay(2200L)
            _trackingState.update {
                if (it.lastActionNotification == message) it.copy(lastActionNotification = null) else it
            }
        }
    }
}
