package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackingState
import com.example.model.UserProfile
import com.example.tracking.TrackingController
import com.example.ui.components.AccessibleCard
import com.example.ui.components.CameraTrackingPreview
import com.example.ui.components.SensitivitySlider

@Composable
fun CalibrationScreen(
    state: TrackingState,
    profile: UserProfile,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) } // 1..4
    val scrollState = rememberScrollState()

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
                    modifier = Modifier.testTag("calibration_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC)
                    )
                }
                Column {
                    Text(
                        text = "Calibration Wizard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Step $step of 4: ${getStepTitle(step)}",
                        fontSize = 12.sp,
                        color = Color(0xFF00E5FF)
                    )
                }
            }
        }

        LinearProgressIndicator(
            progress = { step / 4f },
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF00E5FF),
            trackColor = Color(0xFF1E293B)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                1 -> StepDistanceAndLighting(state)
                2 -> StepRestingCenter(state)
                3 -> StepTremorDeadzone(profile)
                4 -> StepReachAndSensitivity(profile)
            }
        }

        // Bottom Wizard Navigation Buttons
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("wizard_prev_button")
                    ) {
                        Text("Previous", color = Color(0xFF94A3B8))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("wizard_next_button")
                ) {
                    Text(
                        text = if (step < 4) "Next Step" else "Finish Calibration",
                        color = Color(0xFF00242B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getStepTitle(step: Int): String = when (step) {
    1 -> "Distance & Lighting"
    2 -> "Resting Position"
    3 -> "Tremor Deadzone"
    4 -> "Reach & Sensitivity"
    else -> ""
}

@Composable
private fun StepDistanceAndLighting(state: TrackingState) {
    AccessibleCard {
        Text(
            text = "Camera Position & Lighting",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF8FAFC)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mount or prop your phone 30–60 cm (1–2 feet) in front of you. Ensure your hand or pointing finger is well-lit and unobstructed.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(16.dp))

        CameraTrackingPreview(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Icon(
                imageVector = if (state.handDetected) Icons.Default.CheckCircle else Icons.Default.PanTool,
                contentDescription = null,
                tint = if (state.handDetected) Color(0xFF10B981) else Color(0xFFFFD600),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (state.handDetected) "Hand Successfully Detected!" else "Place hand into camera view",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFFF8FAFC)
                )
                Text(
                    text = "Tracking confidence: ${(state.confidence * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun StepRestingCenter(state: TrackingState) {
    AccessibleCard {
        Text(
            text = "Resting Hand Position",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF8FAFC)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hold your hand or finger in the most comfortable, effortless neutral position. AuraMotion will use this as the center anchor point.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Raw Position: (${(state.rawX * 100).toInt()}%, ${(state.rawY * 100).toInt()}%)",
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { TrackingController.recalibrateCenter() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("lock_center_button")
        ) {
            Text("Lock Current Hand As Center", color = Color(0xFF00242B), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StepTremorDeadzone(profile: UserProfile) {
    AccessibleCard {
        Text(
            text = "Tremor & Jitter Damping",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF8FAFC)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "For users with tremors, spasticity, or natural shaking, a larger deadzone and higher smoothing filters ensure the cursor stays locked on targets.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(16.dp))

        SensitivitySlider(
            title = "Tremor Deadzone Radius",
            value = profile.deadZoneRadius * 100f,
            valueRange = 0.5f..12f,
            unit = "%",
            description = "Unintentional movements within this radius are ignored.",
            onValueChange = {
                TrackingController.updateProfile(profile.copy(deadZoneRadius = it / 100f))
            },
            tag = "deadzone_slider"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SensitivitySlider(
            title = "Smoothing Strength",
            value = (1f - profile.smoothingFactor) * 10f,
            valueRange = 2f..9.5f,
            unit = "/10",
            description = "Higher values apply stronger damping to eliminate hand tremors.",
            onValueChange = {
                val smoothing = 1f - (it / 10f)
                TrackingController.updateProfile(profile.copy(smoothingFactor = smoothing))
            },
            tag = "smoothing_slider"
        )
    }
}

@Composable
private fun StepReachAndSensitivity(profile: UserProfile) {
    AccessibleCard {
        Text(
            text = "Screen Reach & Sensitivity",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF8FAFC)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tune horizontal and vertical sensitivity so a gentle hand movement allows you to reach all edges of the screen effortlessly.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(16.dp))

        SensitivitySlider(
            title = "Horizontal Sensitivity (X)",
            value = profile.sensitivityX,
            valueRange = 0.4f..3.5f,
            description = "Higher value requires less horizontal movement.",
            onValueChange = {
                TrackingController.updateProfile(profile.copy(sensitivityX = it))
            },
            tag = "sens_x_slider"
        )

        Spacer(modifier = Modifier.height(12.dp))

        SensitivitySlider(
            title = "Vertical Sensitivity (Y)",
            value = profile.sensitivityY,
            valueRange = 0.4f..3.5f,
            description = "Higher value requires less vertical movement.",
            onValueChange = {
                TrackingController.updateProfile(profile.copy(sensitivityY = it))
            },
            tag = "sens_y_slider"
        )
    }
}
