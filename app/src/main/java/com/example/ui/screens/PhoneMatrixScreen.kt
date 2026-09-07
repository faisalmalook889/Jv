package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstalledAppInfo
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudCard
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun PhoneMatrixScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    var appSearchQuery by remember { mutableStateOf("") }
    var volumeSlider by remember(telemetry.volumePercent) { mutableFloatStateOf(telemetry.volumePercent.toFloat()) }

    val filteredApps = if (appSearchQuery.isBlank()) {
        installedApps.take(24)
    } else {
        installedApps.filter {
            it.appName.contains(appSearchQuery, ignoreCase = true) ||
            it.packageName.contains(appSearchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Hardware Controls HUD
        HudCard(
            title = "Hardware Subsystem Matrix",
            icon = Icons.Default.Settings,
            badge = "ONLINE",
            borderColor = JarvisCyan,
            modifier = Modifier.testTag("hardware_matrix_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Volume Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                        contentDescription = null,
                        tint = JarvisCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volumeSlider,
                        onValueChange = {
                            volumeSlider = it
                            viewModel.setVolumeDirect(it.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisSurfaceElevated
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("volume_slider")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${volumeSlider.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyan,
                        fontSize = 12.sp
                    )
                }

                // Quick Hardware Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingTile(
                        icon = Icons.Default.FlashlightOn,
                        title = "Flashlight",
                        status = if (telemetry.isTorchOn) "ACTIVE" else "OFF",
                        isActive = telemetry.isTorchOn,
                        activeColor = JarvisAmber,
                        onClick = { viewModel.toggleTorchDirect() },
                        modifier = Modifier.weight(1f)
                    )

                    SettingTile(
                        icon = Icons.Default.Wifi,
                        title = "Wi-Fi Hub",
                        status = "SETTINGS",
                        isActive = false,
                        activeColor = JarvisCyan,
                        onClick = { viewModel.automationEngine.openSettings("wifi") },
                        modifier = Modifier.weight(1f)
                    )

                    SettingTile(
                        icon = Icons.Default.Bluetooth,
                        title = "Bluetooth",
                        status = "SETTINGS",
                        isActive = false,
                        activeColor = JarvisCyan,
                        onClick = { viewModel.automationEngine.openSettings("bluetooth") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingTile(
                        icon = Icons.Default.Brightness6,
                        title = "Display",
                        status = "SETTINGS",
                        isActive = false,
                        activeColor = JarvisCyan,
                        onClick = { viewModel.automationEngine.openSettings("display") },
                        modifier = Modifier.weight(1f)
                    )

                    SettingTile(
                        icon = Icons.Default.BatterySaver,
                        title = "Power",
                        status = "${telemetry.batteryPercent}%",
                        isActive = telemetry.isCharging,
                        activeColor = JarvisGreen,
                        onClick = { viewModel.automationEngine.openSettings("battery") },
                        modifier = Modifier.weight(1f)
                    )

                    SettingTile(
                        icon = Icons.Default.Accessibility,
                        title = "System",
                        status = "SETTINGS",
                        isActive = false,
                        activeColor = JarvisCyan,
                        onClick = { viewModel.automationEngine.openSettings("accessibility") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // App Launch & Control Matrix
        HudCard(
            title = "Installed Applications (${installedApps.size})",
            icon = Icons.Default.Apps,
            badge = "REAL-TIME",
            borderColor = JarvisAmber,
            modifier = Modifier.testTag("apps_matrix_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Apps
                OutlinedTextField(
                    value = appSearchQuery,
                    onValueChange = { appSearchQuery = it },
                    placeholder = {
                        Text(
                            text = "Filter or launch application...",
                            color = JarvisTextSecondary.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = JarvisAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisAmber,
                        unfocusedBorderColor = JarvisAmber.copy(alpha = 0.3f),
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary,
                        focusedContainerColor = JarvisSurfaceElevated,
                        unfocusedContainerColor = JarvisSurfaceElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_search_field")
                )

                // App Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    filteredApps.chunked(3).forEach { rowApps ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowApps.forEach { app ->
                                AppGridItem(
                                    app = app,
                                    onLaunch = { viewModel.automationEngine.openApp(app.packageName) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining empty slots in row
                            for (i in 0 until (3 - rowApps.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingTile(
    icon: ImageVector,
    title: String,
    status: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else JarvisSurfaceElevated)
            .border(1.dp, if (isActive) activeColor else JarvisCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isActive) activeColor else JarvisCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextPrimary,
                fontSize = 11.sp
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) activeColor else JarvisTextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun AppGridItem(
    app: InstalledAppInfo,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurfaceElevated.copy(alpha = 0.8f))
            .border(0.5.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .clickable { onLaunch() }
            .padding(horizontal = 6.dp, vertical = 8.dp)
            .testTag("app_tile_${app.appName}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(JarvisCyan.copy(alpha = 0.15f), CircleShape)
            ) {
                Text(
                    text = app.appName.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = JarvisCyan,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.appName,
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextPrimary,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
