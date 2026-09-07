package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SystemTelemetry
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder

@Composable
fun TelemetryBar(
    telemetry: SystemTelemetry,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurface.copy(alpha = 0.7f))
            .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("telemetry_bar")
    ) {
        // Battery
        TelemetryItem(
            icon = Icons.Default.BatteryChargingFull,
            label = "${telemetry.batteryPercent}%",
            color = if (telemetry.batteryPercent > 20) JarvisGreen else JarvisAmber,
            modifier = Modifier.testTag("battery_metric")
        )

        // RAM
        TelemetryItem(
            icon = Icons.Default.Memory,
            label = "${telemetry.ramUsedMb}M",
            color = JarvisCyan,
            modifier = Modifier.testTag("ram_metric")
        )

        // Volume
        TelemetryItem(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            label = "${telemetry.volumePercent}%",
            color = JarvisCyan,
            modifier = Modifier.testTag("volume_metric")
        )

        // Torch Quick Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (telemetry.isTorchOn) JarvisAmber.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { onToggleTorch() }
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("torch_toggle_btn")
        ) {
            Icon(
                imageVector = Icons.Default.FlashlightOn,
                contentDescription = "Torch Toggle",
                tint = if (telemetry.isTorchOn) JarvisAmber else Color.Gray,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (telemetry.isTorchOn) "TORCH" else "OFF",
                style = MaterialTheme.typography.labelSmall,
                color = if (telemetry.isTorchOn) JarvisAmber else Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun TelemetryItem(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 10.sp
        )
    }
}
