package com.example

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackingState
import com.example.model.UserProfile
import com.example.service.ServiceHelper
import com.example.tracking.TrackingController
import com.example.ui.screens.CalibrationScreen
import com.example.ui.screens.CursorCustomizerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GesturesScreen
import com.example.ui.screens.PermissionsGuideScreen
import com.example.ui.screens.PlaygroundScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    PLAYGROUND,
    PROFILES,
    CALIBRATION,
    GESTURES,
    CURSOR,
    GUIDE
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep screen on during hands-free accessibility control
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize tracking engine
        TrackingController.initialize(this)

        setContent {
            MyApplicationTheme {
                AuraMotionApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TrackingController.stopTracking()
    }
}

@Composable
fun AuraMotionApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    val trackingState by TrackingController.trackingState.collectAsState()
    val userProfile by TrackingController.userProfile.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            TrackingController.startTracking()
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission granted. Tracking active.")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission needed for vision hand tracking.")
            }
        }
    }

    // Handle back button on sub-screens
    if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFF090D1A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Standard M3 bottom navigation bar respecting navigation insets
            if (currentScreen in listOf(AppScreen.DASHBOARD, AppScreen.PLAYGROUND, AppScreen.PROFILES, AppScreen.CURSOR)) {
                NavigationBar(
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color(0xFF00E5FF),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { currentScreen = AppScreen.DASHBOARD },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00242B),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PLAYGROUND,
                        onClick = { currentScreen = AppScreen.PLAYGROUND },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Playground") },
                        label = { Text("Playground", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00242B),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_playground")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.PROFILES,
                        onClick = { currentScreen = AppScreen.PROFILES },
                        icon = { Icon(Icons.Default.Tune, contentDescription = "Profiles") },
                        label = { Text("Profiles", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00242B),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_profiles")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CURSOR,
                        onClick = { currentScreen = AppScreen.CURSOR },
                        icon = { Icon(Icons.Default.Visibility, contentDescription = "Cursor") },
                        label = { Text("Cursor", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00242B),
                            selectedTextColor = Color(0xFF00E5FF),
                            indicatorColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_item_cursor")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(
                    state = trackingState,
                    profile = userProfile,
                    onNavigatePlayground = { currentScreen = AppScreen.PLAYGROUND },
                    onNavigateCalibration = { currentScreen = AppScreen.CALIBRATION },
                    onNavigateProfiles = { currentScreen = AppScreen.PROFILES },
                    onNavigateGestures = { currentScreen = AppScreen.GESTURES },
                    onNavigateCursorStyle = { currentScreen = AppScreen.CURSOR },
                    onNavigatePermissionsGuide = { currentScreen = AppScreen.GUIDE },
                    onRequestCameraPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )

                AppScreen.PLAYGROUND -> PlaygroundScreen(
                    state = trackingState,
                    profile = userProfile,
                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                )

                AppScreen.CALIBRATION -> CalibrationScreen(
                    state = trackingState,
                    profile = userProfile,
                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                )

                AppScreen.PROFILES -> ProfilesScreen(
                    profile = userProfile,
                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                )

                AppScreen.GESTURES -> GesturesScreen(
                    profile = userProfile,
                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                )

                AppScreen.CURSOR -> CursorCustomizerScreen(
                    state = trackingState,
                    profile = userProfile,
                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD }
                )

                AppScreen.GUIDE -> PermissionsGuideScreen(
                    onNavigateBack = { currentScreen = AppScreen.DASHBOARD },
                    onRequestCameraPermission = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }
}
