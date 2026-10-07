package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Pinch
import androidx.compose.material.icons.filled.TouchApp
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.tracking.TrackingController
import com.example.ui.components.AccessibleCard

@Composable
fun GesturesScreen(
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
                    modifier = Modifier.testTag("gestures_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFF8FAFC)
                    )
                }
                Column {
                    Text(
                        text = "Gesture Mapping & Hot Edges",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Customize hand commands & shortcuts",
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
                text = "Primary Vision Gestures",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            GestureMappingRow(
                icon = Icons.Default.TouchApp,
                gestureName = "Dwell Hover (Default)",
                assignedAction = "Single Tap / Click",
                description = "Pausing over any on-screen button or link for ${profile.dwellTimeMs}ms executes a tap."
            )

            GestureMappingRow(
                icon = Icons.Default.Pinch,
                gestureName = "Finger Pinch",
                assignedAction = "Instant Click",
                description = "Bringing your thumb and index fingertip together immediately triggers a tap without waiting for dwell."
            )

            GestureMappingRow(
                icon = Icons.Default.FrontHand,
                gestureName = "Fist / Grasp",
                assignedAction = "Toggle Drag & Drop",
                description = "Making a closed fist locks the cursor into drag mode to swipe cards, sliders, or reorder items."
            )

            Text(
                text = "System Air Navigation Swipes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )

            GestureMappingRow(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                gestureName = "Flick Hand Left",
                assignedAction = "System Back Button",
                description = "A rapid horizontal hand flick to the left navigates back."
            )

            GestureMappingRow(
                icon = Icons.Default.ArrowUpward,
                gestureName = "Flick Hand Up",
                assignedAction = "System Home Screen",
                description = "A quick upward hand flick returns to the phone's home screen."
            )

            GestureMappingRow(
                icon = Icons.Default.ArrowDownward,
                gestureName = "Flick Hand Down",
                assignedAction = "Notification Shade",
                description = "A quick downward hand flick pulls down the notifications and quick settings shade."
            )

            // Hot Edges Card
            AccessibleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hot Screen Edges",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Dwelling on the extreme left, bottom, or top edges of the screen triggers Back, Home, and Notifications automatically.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = profile.hotEdgesEnabled,
                        onCheckedChange = {
                            TrackingController.updateProfile(profile.copy(hotEdgesEnabled = it))
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

@Composable
private fun GestureMappingRow(
    icon: ImageVector,
    gestureName: String,
    assignedAction: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gestureName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Surface(
                        color = Color(0xFF004D59),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = assignedAction,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}
