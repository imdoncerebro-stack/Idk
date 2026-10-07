package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackingState
import com.example.model.UserProfile
import com.example.service.AuraAccessibilityService
import com.example.service.AuraOverlayService
import com.example.service.ServiceHelper
import com.example.tracking.TrackingController
import com.example.ui.components.AccessibleCard
import com.example.ui.components.CameraTrackingPreview
import com.example.ui.components.MetricTile
import com.example.ui.components.PermissionStatusRow

@Composable
fun DashboardScreen(
    state: TrackingState,
    profile: UserProfile,
    onNavigatePlayground: () -> Unit,
    onNavigateCalibration: () -> Unit,
    onNavigateProfiles: () -> Unit,
    onNavigateGestures: () -> Unit,
    onNavigateCursorStyle: () -> Unit,
    onNavigatePermissionsGuide: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val isCameraGranted = ServiceHelper.isCameraPermissionGranted(context)
    val canDrawOverlays = ServiceHelper.canDrawOverlays(context)
    val isAccessibilityOn = AuraAccessibilityService.isRunning

    val masterButtonColor by animateColorAsState(
        targetValue = if (state.isTrackingActive) Color(0xFF10B981) else Color(0xFF00E5FF),
        label = "masterButtonColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D1A))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AuraMotion",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF)
                )
                Text(
                    text = "Hands-Free Device Control",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Notification indicator for actions
            state.lastActionNotification?.let { msg ->
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Master Control Card
        AccessibleCard(
            borderColor = if (state.isTrackingActive) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF00E5FF).copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.isTrackingActive) "HANDS-FREE CONTROL ACTIVE" else "HANDS-FREE CONTROL PAUSED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (state.isTrackingActive) Color(0xFF10B981) else Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Big Master Toggle Button
                Button(
                    onClick = {
                        if (!isCameraGranted) {
                            onRequestCameraPermission()
                        } else {
                            val active = TrackingController.toggleTracking()
                            if (active && canDrawOverlays) {
                                ServiceHelper.startOverlayService(context)
                            } else if (!active) {
                                ServiceHelper.stopOverlayService(context)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("master_tracking_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = masterButtonColor),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (state.isTrackingActive) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF00242B),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (state.isTrackingActive) "Stop Hands-Free Tracking" else "Start Hands-Free Tracking",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00242B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = state.statusMessage,
                    fontSize = 13.sp,
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Live Camera Preview & Real-Time Tracking HUD
        if (isCameraGranted) {
            AccessibleCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (state.handDetected) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.handDetected) "Hand Locked (${state.currentGesture.label})" else "Searching for Hand...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF8FAFC)
                            )
                        }

                        Text(
                            text = "${state.fps} FPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Front Camera tracking feed
                    CameraTrackingPreview(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricTile(label = "Pose", value = state.currentGesture.label.take(10), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        MetricTile(label = "Dwell", value = "${(state.dwellProgress * 100).toInt()}%", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        MetricTile(label = "Latency", value = "${state.latencyMs}ms", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Quick Navigation to Interactive Playground & Target Practice
        Button(
            onClick = onNavigatePlayground,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("open_playground_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = Color(0xFF1E1B4B),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Interactive Gesture Playground",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
            }
        }

        // Active Mobility Profile Selector Card
        AccessibleCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Mobility Profile",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = profile.presetName,
                        fontSize = 13.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onNavigateProfiles,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("manage_profiles_button")
                ) {
                    Text("Change", color = Color(0xFF00E5FF), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset Quick Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip(
                    title = "Tremor Relief",
                    isSelected = profile.presetName == UserProfile.TREMOR_RELIEF.presetName,
                    onClick = { TrackingController.selectPreset(UserProfile.TREMOR_RELIEF) }
                )
                PresetChip(
                    title = "Low Range",
                    isSelected = profile.presetName == UserProfile.LIMITED_RANGE_OF_MOTION.presetName,
                    onClick = { TrackingController.selectPreset(UserProfile.LIMITED_RANGE_OF_MOTION) }
                )
                PresetChip(
                    title = "Balanced",
                    isSelected = profile.presetName == UserProfile.BALANCED.presetName,
                    onClick = { TrackingController.selectPreset(UserProfile.BALANCED) }
                )
            }
        }

        // System Readiness & Permissions Section
        Text(
            text = "Accessibility System Readiness",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF8FAFC)
        )

        // 1. Camera Permission
        PermissionStatusRow(
            title = "Front Camera Access",
            description = if (isCameraGranted) "Granted. Real-time vision ready." else "Required to track hand gestures.",
            isGranted = isCameraGranted,
            actionButtonText = "Grant",
            onActionClick = onRequestCameraPermission,
            tag = "grant_camera_button"
        )

        // 2. Floating Cursor Overlay
        PermissionStatusRow(
            title = "Display Over Other Apps",
            description = if (canDrawOverlays) "Granted. Floating cursor visible across all apps." else "Allows cursor overlay over apps and home screen.",
            isGranted = canDrawOverlays,
            actionButtonText = "Enable",
            onActionClick = { ServiceHelper.openOverlaySettings(context) },
            tag = "enable_overlay_button"
        )

        // 3. Accessibility Service
        PermissionStatusRow(
            title = "Accessibility Service",
            description = if (isAccessibilityOn) "Active. Synthetic clicks, scrolls, and navigation ready." else "Enables hands-free taps, scrolls, and Home/Back actions.",
            isGranted = isAccessibilityOn,
            actionButtonText = "Setup",
            onActionClick = { ServiceHelper.openAccessibilitySettings(context) },
            tag = "enable_accessibility_button"
        )

        // Navigation Grid (Settings, Calibration, Cursor, Gestures)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavFeatureTile(
                icon = Icons.Default.Tune,
                title = "Calibration",
                subtitle = "Resting center & reach",
                onClick = onNavigateCalibration,
                modifier = Modifier.weight(1f),
                tag = "nav_calibration_tile"
            )
            NavFeatureTile(
                icon = Icons.Default.Visibility,
                title = "Cursor Style",
                subtitle = "Size, pointer & ring",
                onClick = onNavigateCursorStyle,
                modifier = Modifier.weight(1f),
                tag = "nav_cursor_style_tile"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavFeatureTile(
                icon = Icons.Default.PanTool,
                title = "Gesture Map",
                subtitle = "Pinch, swipe & dwell",
                onClick = onNavigateGestures,
                modifier = Modifier.weight(1f),
                tag = "nav_gestures_tile"
            )
            NavFeatureTile(
                icon = Icons.Default.OpenInBrowser,
                title = "Assist Guide",
                subtitle = "Setup instructions",
                onClick = onNavigatePermissionsGuide,
                modifier = Modifier.weight(1f),
                tag = "nav_guide_tile"
            )
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF004D59) else Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF00E5FF) else Color.Transparent
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun NavFeatureTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .testTag(tag),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
