package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CursorStyle
import com.example.model.TrackingState
import com.example.model.UserProfile
import com.example.tracking.TrackingController
import com.example.ui.components.AccessibleCard
import com.example.ui.components.CursorOverlayComposable
import com.example.ui.components.SensitivitySlider

val CursorColors = listOf(
    Pair("Cyan", 0xFF00E5FF),
    Pair("Amber", 0xFFFFD600),
    Pair("Violet", 0xFFB388FF),
    Pair("Emerald", 0xFF10B981),
    Pair("Coral", 0xFFFF5252),
    Pair("White", 0xFFFFFFFF)
)

@Composable
fun CursorCustomizerScreen(
    state: TrackingState,
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
                    modifier = Modifier.testTag("cursor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC)
                    )
                }
                Column {
                    Text(
                        text = "Cursor Customizer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "High-visibility pointer styles & colors",
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
            // Live Preview Box
            AccessibleCard {
                Text(
                    text = "LIVE CURSOR PREVIEW",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF050811))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                ) {
                    // Preview cursor centered
                    CursorOverlayComposable(
                        state = state.copy(
                            isTrackingActive = true,
                            handDetected = true,
                            normalizedX = 0.5f,
                            normalizedY = 0.5f,
                            dwellProgress = 0.65f
                        ),
                        profile = profile,
                        modifier = Modifier.fillMaxSize()
                    )

                    Text(
                        text = "Simulated Dwell (65%)",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }

            // Pointer Style Selector
            Text(
                text = "Pointer Graphic Style",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CursorStyle.entries.forEach { style ->
                    val isSelected = profile.cursorStyle == style
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                TrackingController.updateProfile(profile.copy(cursorStyle = style))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = style.displayName,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFF8FAFC),
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }
            }

            // Color Palette
            Text(
                text = "High-Contrast Accent Color",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CursorColors.forEach { (name, hex) ->
                    val isSelected = profile.cursorColorHex == hex
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .border(
                                width = if (isSelected) 3.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                TrackingController.updateProfile(profile.copy(cursorColorHex = hex))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = if (hex == 0xFFFFFFFF) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Cursor Size Slider
            SensitivitySlider(
                title = "Pointer Size",
                value = profile.cursorSizeDp.toFloat(),
                valueRange = 28f..68f,
                unit = "dp",
                description = "Adjust size for clarity on large or high-density displays.",
                onValueChange = {
                    TrackingController.updateProfile(profile.copy(cursorSizeDp = it.toInt()))
                },
                tag = "cursor_size_slider"
            )

            // Dwell Ring Toggle
            AccessibleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Circular Dwell Countdown Ring",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Displays smooth clockwise radial filling progress when hovering over targets.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = profile.showDwellRing,
                        onCheckedChange = {
                            TrackingController.updateProfile(profile.copy(showDwellRing = it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF004D59)
                        )
                    )
                }
            }
        }
    }
}
