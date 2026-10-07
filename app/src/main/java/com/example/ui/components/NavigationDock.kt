package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DwellAction
import com.example.model.TrackingState
import com.example.service.AuraAccessibilityService
import com.example.tracking.TrackingController

/**
 * High-contrast accessibility navigation dock providing instant, one-dwell access
 * to core phone navigation actions: Home, Back, Recents, Scrolling, and Calibration.
 */
@Composable
fun NavigationDock(
    state: TrackingState,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(true) }
    val service = AuraAccessibilityService.instance

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF818CF8))),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("navigation_dock_surface"),
        color = Color(0xFF0F172A).copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Navigation Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                DockActionButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    label = "Back",
                    tag = "dock_back_button",
                    onClick = {
                        val handled = service?.performBack() ?: false
                        if (!handled && onNavigateBack != null) {
                            onNavigateBack()
                        } else {
                            TrackingController.notifyAction("Back Triggered")
                        }
                    }
                )

                // Home Button
                DockActionButton(
                    icon = Icons.Default.Home,
                    label = "Home",
                    tag = "dock_home_button",
                    onClick = {
                        val handled = service?.performHome() ?: false
                        TrackingController.notifyAction(if (handled) "Home Screen" else "Home (Accessibility Service required)")
                    }
                )

                // Recents Button
                DockActionButton(
                    icon = Icons.Default.ViewCarousel,
                    label = "Recents",
                    tag = "dock_recents_button",
                    onClick = {
                        val handled = service?.performRecents() ?: false
                        TrackingController.notifyAction(if (handled) "App Switcher" else "Recents (Accessibility Service required)")
                    }
                )

                // Scroll Up Button
                DockActionButton(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "Scroll ▲",
                    tag = "dock_scroll_up_button",
                    onClick = {
                        service?.scroll(up = true)
                        TrackingController.notifyAction("Scrolled Up")
                    }
                )

                // Scroll Down Button
                DockActionButton(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "Scroll ▼",
                    tag = "dock_scroll_down_button",
                    onClick = {
                        service?.scroll(up = false)
                        TrackingController.notifyAction("Scrolled Down")
                    }
                )

                // Calibrate Center Button
                DockActionButton(
                    icon = Icons.Default.CenterFocusStrong,
                    label = "Center",
                    tag = "dock_calibrate_button",
                    accentColor = Color(0xFFFFD600),
                    onClick = {
                        TrackingController.recalibrateCenter()
                    }
                )
            }

            // Secondary Dwell Mode Selector Pill Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DwellActionChip(
                    action = DwellAction.TAP,
                    isSelected = state.activeDwellAction == DwellAction.TAP,
                    onClick = { TrackingController.setActiveDwellAction(DwellAction.TAP) }
                )
                DwellActionChip(
                    action = DwellAction.DOUBLE_TAP,
                    isSelected = state.activeDwellAction == DwellAction.DOUBLE_TAP,
                    onClick = { TrackingController.setActiveDwellAction(DwellAction.DOUBLE_TAP) }
                )
                DwellActionChip(
                    action = DwellAction.LONG_PRESS,
                    isSelected = state.activeDwellAction == DwellAction.LONG_PRESS,
                    onClick = { TrackingController.setActiveDwellAction(DwellAction.LONG_PRESS) }
                )
                DwellActionChip(
                    action = DwellAction.DRAG_TOGGLE,
                    isSelected = state.activeDwellAction == DwellAction.DRAG_TOGGLE,
                    onClick = { TrackingController.setActiveDwellAction(DwellAction.DRAG_TOGGLE) }
                )
            }
        }
    }
}

@Composable
private fun DockActionButton(
    icon: ImageVector,
    label: String,
    tag: String,
    accentColor: Color = Color(0xFF00E5FF),
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(tag)
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
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFF8FAFC),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun DwellActionChip(
    action: DwellAction,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFF004D59) else Color(0xFF1E293B)
    val textCol = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8)
    val borderCol = if (isSelected) Color(0xFF00E5FF) else Color.Transparent

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("dwell_chip_${action.name}")
    ) {
        Text(
            text = action.title,
            fontSize = 10.sp,
            color = textCol
        )
    }
}
