package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.CursorStyle
import com.example.model.DwellAction
import com.example.tracking.TrackingController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Foreground Service that maintains the system-wide floating cursor overlay
 * and quick-navigation dock across any Android application.
 */
class AuraOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var stateCollectJob: Job? = null

    private var windowManager: WindowManager? = null
    private var cursorOverlayView: CursorOverlayView? = null
    private var cursorLayoutParams: WindowManager.LayoutParams? = null

    companion object {
        const val CHANNEL_ID = "auramotion_overlay_channel"
        const val NOTIFICATION_ID = 4040
        var isRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        initCursorOverlay()
        observeTrackingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        stateCollectJob?.cancel()

        cursorOverlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
        }
        cursorOverlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_desc))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun initCursorOverlay() {
        cursorOverlayView = CursorOverlayView(this)

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Full screen transparent click-through canvas overlay
        cursorLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager?.addView(cursorOverlayView, cursorLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeTrackingState() {
        stateCollectJob = serviceScope.launch {
            TrackingController.trackingState.collect { state ->
                cursorOverlayView?.updateState(
                    screenX = state.screenX,
                    screenY = state.screenY,
                    dwellProgress = state.dwellProgress,
                    handDetected = state.handDetected,
                    isTrackingActive = state.isTrackingActive
                )
            }
        }
    }

    /**
     * Custom view drawing the cursor reticle, dwell circle, and indicator rings.
     */
    class CursorOverlayView(context: Context) : View(context) {

        private var cursorX = 540f
        private var cursorY = 1170f
        private var dwellProgress = 0f
        private var handDetected = false
        private var isTrackingActive = false

        private val outerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0xFF00E5FF.toInt()
        }

        private val dwellArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 7f
            color = 0xFFFFD600.toInt()
            strokeCap = Paint.Cap.ROUND
        }

        private val centerDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFFFFFFFF.toInt()
        }

        private val haloGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x3300E5FF
        }

        private val arcBounds = RectF()

        fun updateState(
            screenX: Float,
            screenY: Float,
            dwellProgress: Float,
            handDetected: Boolean,
            isTrackingActive: Boolean
        ) {
            this.cursorX = screenX
            this.cursorY = screenY
            this.dwellProgress = dwellProgress
            this.handDetected = handDetected
            this.isTrackingActive = isTrackingActive
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (!isTrackingActive || !handDetected) return

            val radius = 32f

            // Halo glow
            canvas.drawCircle(cursorX, cursorY, radius + 12f, haloGlowPaint)

            // Base reticle ring
            canvas.drawCircle(cursorX, cursorY, radius, outerCirclePaint)

            // Center tracking dot
            canvas.drawCircle(cursorX, cursorY, 6f, centerDotPaint)

            // Crosshair ticks
            canvas.drawLine(cursorX - radius - 10f, cursorY, cursorX - radius + 4f, cursorY, outerCirclePaint)
            canvas.drawLine(cursorX + radius - 4f, cursorY, cursorX + radius + 10f, cursorY, outerCirclePaint)
            canvas.drawLine(cursorX, cursorY - radius - 10f, cursorX, cursorY - radius + 4f, outerCirclePaint)
            canvas.drawLine(cursorX, cursorY + radius - 4f, cursorX, cursorY + radius + 10f, outerCirclePaint)

            // Dwell progress ring
            if (dwellProgress > 0f) {
                arcBounds.set(cursorX - radius, cursorY - radius, cursorX + radius, cursorY + radius)
                canvas.drawArc(arcBounds, -90f, dwellProgress * 360f, false, dwellArcPaint)
            }
        }
    }
}
