package com.example.ui.components

import android.content.Context
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.TrackingState
import com.example.tracking.TrackingController
import java.util.concurrent.Executors

/**
 * Camera preview with real-time hand-tracking computer vision overlay.
 */
@Composable
fun CameraTrackingPreview(
    state: TrackingState,
    modifier: Modifier = Modifier,
    isPip: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isFrontCamera by remember { mutableStateOf(true) }
    var cameraAvailable by remember { mutableStateOf(true) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B1020))
            .testTag("camera_tracking_preview")
    ) {
        if (cameraAvailable) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(480, 640))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                TrackingController.onCameraFrame(imageProxy, isFrontCamera)
                                imageProxy.close()
                            }

                            val cameraSelector = if (isFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            cameraAvailable = false
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Camera fallback box
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = "Camera unavailable",
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Computer Vision Landmark HUD Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (state.handDetected) {
                val fx = state.rawX * size.width
                val fy = state.rawY * size.height

                // Hand detection tracking target
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 24.dp.toPx(),
                    center = Offset(fx, fy),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = Offset(fx, fy)
                )

                // Status text / gesture badge
                drawCircle(
                    color = if (state.isDwellTriggered) Color(0xFF10B981) else Color(0xFFFFD600),
                    radius = 10.dp.toPx(),
                    center = Offset(fx + 24.dp.toPx(), fy - 24.dp.toPx())
                )
            }
        }

        // Top Status Badge (FPS / Latency)
        Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Text(
                text = "${state.fps} FPS • ${state.latencyMs}ms",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        // Camera Switch Button
        if (!isPip) {
            IconButton(
                onClick = { isFrontCamera = !isFrontCamera },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("switch_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }
        }
    }
}
