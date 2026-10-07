package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AuraAccessibilityService
import com.example.service.ServiceHelper
import com.example.ui.components.AccessibleCard

@Composable
fun PermissionsGuideScreen(
    onNavigateBack: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isCameraGranted = ServiceHelper.isCameraPermissionGranted(context)
    val canDrawOverlays = ServiceHelper.canDrawOverlays(context)
    val isAccessibilityOn = AuraAccessibilityService.isRunning

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D1A))
    ) {
        // Top Header
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("guide_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC)
                    )
                }
                Column {
                    Text(
                        text = "Assistive Setup Guide",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Why these permissions empower hands-free control",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy & On-Device Processing Notice
            AccessibleCard(borderColor = Color(0xFF00E5FF).copy(alpha = 0.4f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% On-Device & Private",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Camera video is processed entirely in local memory on your phone. No images or video are ever recorded, saved, or uploaded to any server.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Step 1: Camera
            GuideStepCard(
                stepNum = "1",
                icon = Icons.Default.CameraAlt,
                title = "Front Camera Tracking",
                isCompleted = isCameraGranted,
                explanation = "AuraMotion needs access to the front camera to optically track hand position, pointing finger direction, and pinch gestures.",
                buttonText = "Grant Camera Permission",
                onAction = onRequestCameraPermission,
                tag = "grant_camera_guide_button"
            )

            // Step 2: Overlay
            GuideStepCard(
                stepNum = "2",
                icon = Icons.Default.Layers,
                title = "Display Over Other Apps",
                isCompleted = canDrawOverlays,
                explanation = "Allows AuraMotion to draw the mouse cursor, dwell countdown ring, and navigation dock on top of any app, the browser, and the home screen.",
                buttonText = "Open Overlay Settings",
                onAction = { ServiceHelper.openOverlaySettings(context) },
                tag = "open_overlay_guide_button"
            )

            // Step 3: Accessibility
            GuideStepCard(
                stepNum = "3",
                icon = Icons.Default.Accessibility,
                title = "Android Accessibility Service",
                isCompleted = isAccessibilityOn,
                explanation = "Enables AuraMotion to convert your hand dwell hovers into synthetic screen taps and scrolls, and execute global commands (Home, Back, Recents). In Settings, find 'AuraMotion' and toggle it ON.",
                buttonText = "Open Accessibility Settings",
                onAction = { ServiceHelper.openAccessibilitySettings(context) },
                tag = "open_accessibility_guide_button"
            )
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNum: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isCompleted: Boolean,
    explanation: String,
    buttonText: String,
    onAction: () -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isCompleted) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFF1E293B),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) Color(0xFF10B981) else Color(0xFF004D59)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = stepNum,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = if (isCompleted) "Ready & Active" else "Setup Required",
                        fontSize = 12.sp,
                        color = if (isCompleted) Color(0xFF10B981) else Color(0xFFFFD600),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = explanation,
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )

            if (!isCompleted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(tag)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = buttonText,
                            color = Color(0xFF00242B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color(0xFF00242B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
