package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisConfirmationDialog
import com.example.ui.screens.CommandLogsScreen
import com.example.ui.screens.JarvisHomeScreen
import com.example.ui.screens.PhoneMatrixScreen
import com.example.ui.screens.ProductivityMemoryScreen
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JarvisAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JarvisAppRoot(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val pendingSecurityPrompt by viewModel.pendingSecurityPrompt.collectAsState()
    var isVoiceOutputMuted by remember { mutableStateOf(!viewModel.voiceEngine.isTtsEnabled) }

    // Audio recording permission request
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Security Confirmation Dialog
    if (pendingSecurityPrompt != null) {
        JarvisConfirmationDialog(
            prompt = pendingSecurityPrompt!!,
            onConfirm = { viewModel.confirmSecurityAction() },
            onDismiss = { viewModel.dismissSecurityAction() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(JarvisGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "J.A.R.V.I.S.",
                            style = MaterialTheme.typography.titleLarge,
                            color = JarvisCyan,
                            letterSpacing = 3.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OS v3.5",
                            style = MaterialTheme.typography.labelSmall,
                            color = JarvisAmber,
                            fontSize = 10.sp
                        )
                    }
                },
                actions = {
                    // TTS Voice Output Mute Toggle
                    IconButton(
                        onClick = {
                            viewModel.voiceEngine.isTtsEnabled = !viewModel.voiceEngine.isTtsEnabled
                            if (!viewModel.voiceEngine.isTtsEnabled) {
                                viewModel.voiceEngine.stopSpeaking()
                            }
                            isVoiceOutputMuted = !viewModel.voiceEngine.isTtsEnabled
                        },
                        modifier = Modifier.testTag("tts_mute_toggle")
                    ) {
                        Icon(
                            imageVector = if (isVoiceOutputMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "TTS Voice Toggle",
                            tint = if (isVoiceOutputMuted) Color.Gray else JarvisCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisBackground,
                    titleContentColor = JarvisCyan
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = JarvisSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, JarvisSurfaceBorder)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GraphicEq, contentDescription = "Terminal") },
                    label = { Text("TERMINAL", fontSize = 10.sp, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan,
                        unselectedIconColor = JarvisTextSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = JarvisTextSecondary.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_terminal")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "Matrix") },
                    label = { Text("MATRIX", fontSize = 10.sp, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan,
                        unselectedIconColor = JarvisTextSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = JarvisTextSecondary.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_matrix")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Memory") },
                    label = { Text("MEMORY", fontSize = 10.sp, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan,
                        unselectedIconColor = JarvisTextSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = JarvisTextSecondary.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_memory")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Logs") },
                    label = { Text("LOGS", fontSize = 10.sp, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = JarvisCyan,
                        indicatorColor = JarvisCyan,
                        unselectedIconColor = JarvisTextSecondary.copy(alpha = 0.6f),
                        unselectedTextColor = JarvisTextSecondary.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_logs")
                )
            }
        },
        containerColor = JarvisBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> JarvisHomeScreen(viewModel = viewModel)
                1 -> PhoneMatrixScreen(viewModel = viewModel)
                2 -> ProductivityMemoryScreen(viewModel = viewModel)
                3 -> CommandLogsScreen(viewModel = viewModel)
            }
        }
    }
}
