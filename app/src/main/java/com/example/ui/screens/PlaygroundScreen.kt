package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GestureType
import com.example.model.TrackingState
import com.example.model.UserProfile
import com.example.tracking.TrackingController
import com.example.ui.components.CameraTrackingPreview
import com.example.ui.components.CursorOverlayComposable
import com.example.ui.components.NavigationDock
import kotlin.math.hypot
import kotlin.random.Random

data class TargetBubble(
    val id: Int,
    val x: Float, // 0.1f .. 0.9f
    val y: Float, // 0.15f .. 0.75f
    val radiusDp: Int = 42,
    val color: Color = Color(0xFF00E5FF),
    val label: String
)

@Composable
fun PlaygroundScreen(
    state: TrackingState,
    profile: UserProfile,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Target Practice, 1: Scroll Zone, 2: Simulated Pad
    var score by remember { mutableIntStateOf(0) }
    var showPipCamera by remember { mutableStateOf(true) }

    // Targets for Bubble Popping
    val bubbles = remember {
        mutableStateListOf(
            TargetBubble(1, 0.25f, 0.28f, 44, Color(0xFF00E5FF), "Target 1"),
            TargetBubble(2, 0.75f, 0.35f, 48, Color(0xFF818CF8), "Target 2"),
            TargetBubble(3, 0.50f, 0.55f, 50, Color(0xFFFFD600), "Target 3"),
            TargetBubble(4, 0.30f, 0.70f, 42, Color(0xFF10B981), "Target 4")
        )
    }

    // Auto-pop bubble if cursor dwells over it
    LaunchedEffect(state.normalizedX, state.normalizedY, state.isDwellTriggered) {
        if (state.isDwellTriggered || state.currentGesture == GestureType.PINCH) {
            val hitIndex = bubbles.indexOfFirst { b ->
                val dist = hypot(state.normalizedX - b.x, state.normalizedY - b.y)
                dist < 0.09f
            }
            if (hitIndex != -1) {
                val hitBubble = bubbles[hitIndex]
                score += 100
                TrackingController.notifyAction("Popped ${hitBubble.label}! (+100)")
                // Respawn target elsewhere
                val random = Random(System.currentTimeMillis())
                bubbles[hitIndex] = hitBubble.copy(
                    x = random.nextFloat() * 0.7f + 0.15f,
                    y = random.nextFloat() * 0.5f + 0.20f
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D1A))
    ) {
        val maxWidthPx = constraints.maxWidth
        val maxHeightPx = constraints.maxHeight

        LaunchedEffect(maxWidthPx, maxHeightPx) {
            TrackingController.updateScreenDimensions(maxWidthPx, maxHeightPx)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("playground_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFFF8FAFC)
                            )
                        }
                        Text(
                            text = "Gesture Playground",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // PIP Camera Toggle
                        IconButton(
                            onClick = { showPipCamera = !showPipCamera },
                            modifier = Modifier.testTag("toggle_pip_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Toggle Camera PIP",
                                tint = if (showPipCamera) Color(0xFF00E5FF) else Color(0xFF64748B)
                            )
                        }

                        // Simulation toggle (automated sweep)
                        IconButton(
                            onClick = {
                                if (state.isSimulatorMode) {
                                    TrackingController.stopSimulator()
                                } else {
                                    TrackingController.startAutomatedSimulatorDemo()
                                }
                            },
                            modifier = Modifier.testTag("toggle_sim_demo_button")
                        ) {
                            Icon(
                                imageVector = if (state.isSimulatorMode) Icons.Default.Stop else Icons.Default.FastForward,
                                contentDescription = "Toggle Demo Sweep",
                                tint = if (state.isSimulatorMode) Color(0xFF10B981) else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Mode Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF00E5FF),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFF00E5FF)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Target Practice", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Scroll Zone", fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Touch Trackpad", fontSize = 13.sp) }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> TargetPracticeCanvas(bubbles = bubbles, score = score)
                    1 -> ScrollZoneView()
                    2 -> VirtualTrackpadView()
                }

                // Mini PIP Camera View
                if (showPipCamera) {
                    CameraTrackingPreview(
                        state = state,
                        isPip = true,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(110.dp, 140.dp)
                            .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    )
                }
            }

            // Floating Navigation Dock
            NavigationDock(
                state = state,
                onNavigateBack = onNavigateBack,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        // Live Rendered Cursor Overlay directly on playground canvas!
        CursorOverlayComposable(
            state = state,
            profile = profile,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun TargetPracticeCanvas(
    bubbles: List<TargetBubble>,
    score: Int
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight

        // Score Card
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(text = "ACCURACY SCORE", fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Text(text = "$score pts", fontSize = 22.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.ExtraBold)
                Text(text = "Hover to Dwell Click or Pinch", fontSize = 11.sp, color = Color(0xFFFFD600))
            }
        }

        // Render Bubbles
        bubbles.forEach { bubble ->
            val offsetX = width * bubble.x - (bubble.radiusDp.dp / 2)
            val offsetY = height * bubble.y - (bubble.radiusDp.dp / 2)

            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .size(bubble.radiusDp.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(bubble.color, bubble.color.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                    .border(2.dp, bubble.color, CircleShape)
                    .clickable {
                        TrackingController.notifyAction("Target tapped!")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bubble.label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ScrollZoneView() {
    val items = remember {
        List(25) { index ->
            "Accessibility News & Tip #${index + 1}: Hands-free device navigation empowers independent living through optical computer vision."
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📜 Hands-Free Scroll Testing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = "Use the bottom dock 'Scroll ▲' and 'Scroll ▼' buttons or vertical hand flick gestures to scroll through this feed.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        itemsIndexed(items) { idx, text ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF004D59)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "${idx + 1}", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = text,
                        fontSize = 13.sp,
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VirtualTrackpadView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val normX = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        val normY = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                        TrackingController.onSimulatorInput(normX, normY)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val normX = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        val normY = (change.position.y / size.height.toFloat()).coerceIn(0f, 1f)
                        TrackingController.onSimulatorInput(normX, normY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Virtual Touch / Testing Trackpad",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Drag your finger across this surface to test cursor movement, tremor smoothing, and dwell clicking directly on emulator or without a camera.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
