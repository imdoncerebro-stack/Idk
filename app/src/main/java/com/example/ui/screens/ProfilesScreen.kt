package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackingMode
import com.example.model.UserProfile
import com.example.tracking.TrackingController
import com.example.ui.components.AccessibleCard
import com.example.ui.components.SensitivitySlider

@Composable
fun ProfilesScreen(
    profile: UserProfile,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    modifier = Modifier.testTag("profiles_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC)
                    )
                }
                Column {
                    Text(
                        text = "Mobility Profiles & Tuning",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Tailored presets for specific motor needs",
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
            Text(
                text = "Select Preset Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            // Preset Cards
            PresetCard(
                title = "Tremor Relief",
                description = "Heavy jitter smoothing, large resting deadzone, and extended dwell stability. Ideal for Parkinson's, essential tremor, or spasticity.",
                isSelected = profile.presetName == UserProfile.TREMOR_RELIEF.presetName,
                accentColor = Color(0xFFFFD600),
                onClick = { TrackingController.selectPreset(UserProfile.TREMOR_RELIEF) }
            )

            PresetCard(
                title = "Low Fatigue / Limited Range",
                description = "High sensitivity relative joystick mode requiring minimal physical wrist or finger displacement. Ideal for ALS or high-level spinal injury.",
                isSelected = profile.presetName == UserProfile.LIMITED_RANGE_OF_MOTION.presetName,
                accentColor = Color(0xFFB388FF),
                onClick = { TrackingController.selectPreset(UserProfile.LIMITED_RANGE_OF_MOTION) }
            )

            PresetCard(
                title = "Balanced Standard",
                description = "Natural response curve suitable for everyday one-hand or finger pointing.",
                isSelected = profile.presetName == UserProfile.BALANCED.presetName,
                accentColor = Color(0xFF00E5FF),
                onClick = { TrackingController.selectPreset(UserProfile.BALANCED) }
            )

            PresetCard(
                title = "High Precision",
                description = "Fine-grained control with target crosshair for clicking tiny links, web forms, and detailed icons.",
                isSelected = profile.presetName == UserProfile.PRECISION_CLICKER.presetName,
                accentColor = Color(0xFF38BDF8),
                onClick = { TrackingController.selectPreset(UserProfile.PRECISION_CLICKER) }
            )

            // Fine-Tuning Sliders
            Text(
                text = "Fine-Tune Profile Parameters",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            SensitivitySlider(
                title = "Dwell Hover Duration",
                value = (profile.dwellTimeMs / 100).toFloat(),
                valueRange = 4f..25f,
                unit = "00 ms",
                description = "How long the cursor must pause over a target to trigger an automatic click.",
                onValueChange = {
                    TrackingController.updateProfile(profile.copy(dwellTimeMs = (it * 100).toLong()))
                },
                tag = "dwell_time_slider"
            )

            SensitivitySlider(
                title = "Dwell Target Tolerance",
                value = profile.dwellToleranceRadius * 100f,
                valueRange = 1.5f..15f,
                unit = "% radius",
                description = "The target zone size where hand shaking will not cancel the dwell countdown.",
                onValueChange = {
                    TrackingController.updateProfile(profile.copy(dwellToleranceRadius = it / 100f))
                },
                tag = "dwell_tolerance_slider"
            )

            SensitivitySlider(
                title = "Horizontal Sensitivity (X)",
                value = profile.sensitivityX,
                valueRange = 0.5f..3.5f,
                unit = "x",
                onValueChange = {
                    TrackingController.updateProfile(profile.copy(sensitivityX = it))
                },
                tag = "sens_x_profile_slider"
            )

            SensitivitySlider(
                title = "Vertical Sensitivity (Y)",
                value = profile.sensitivityY,
                valueRange = 0.5f..3.5f,
                unit = "x",
                onValueChange = {
                    TrackingController.updateProfile(profile.copy(sensitivityY = it))
                },
                tag = "sens_y_profile_slider"
            )

            // Toggles
            AccessibleCard {
                Text(
                    text = "Feedback & Assistance Toggles",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToggleRow(
                    title = "Haptic Vibration on Click",
                    subtitle = "Gives physical confirmation when dwell or pinch fires",
                    checked = profile.hapticFeedbackEnabled,
                    onCheckedChange = {
                        TrackingController.updateProfile(profile.copy(hapticFeedbackEnabled = it))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToggleRow(
                    title = "Pinch-to-Click Gesture",
                    subtitle = "Instantly tap by touching thumb and index finger",
                    checked = profile.pinchClickEnabled,
                    onCheckedChange = {
                        TrackingController.updateProfile(profile.copy(pinchClickEnabled = it))
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToggleRow(
                    title = "Circular Dwell Ring Indicator",
                    subtitle = "Visual clockwise radial filling ring around cursor",
                    checked = profile.showDwellRing,
                    onCheckedChange = {
                        TrackingController.updateProfile(profile.copy(showDwellRing = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isSelected) accentColor else Color(0xFF1E293B),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color(0xFFF8FAFC)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF8FAFC))
            Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF94A3B8))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00E5FF),
                checkedTrackColor = Color(0xFF004D59)
            )
        )
    }
}
