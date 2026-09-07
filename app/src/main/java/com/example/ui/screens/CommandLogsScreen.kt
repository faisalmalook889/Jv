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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CommandLogEntity
import com.example.ui.JarvisViewModel
import com.example.ui.components.HudCard
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandLogsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.commandLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HudCard(
            title = "Telemetry & Execution Log Archive (${logs.size})",
            icon = Icons.Default.History,
            badge = "SECURE",
            borderColor = JarvisCyan,
            modifier = Modifier.testTag("command_logs_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (logs.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.clearCommandLogs() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisRed.copy(alpha = 0.2f), contentColor = JarvisRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("clear_logs_btn")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PURGE LOGS", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (logs.isEmpty()) {
                    Text(
                        text = "No recorded voice commands yet. Issue a voice command to view execution latency, sub-task breakdowns, and action telemetry.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                } else {
                    logs.forEach { logItem ->
                        LogCardItem(
                            log = logItem,
                            onRerun = { viewModel.executeVoiceCommand(logItem.rawCommand) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LogCardItem(
    log: CommandLogEntity,
    onRerun: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = dateFormat.format(Date(log.timestamp))

    val statusColor = when (log.status) {
        "SUCCESS" -> JarvisGreen
        "PARTIAL" -> JarvisAmber
        else -> JarvisRed
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurfaceElevated)
            .border(0.5.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("log_item_${log.id}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (log.status == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = log.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontSize = 10.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Latency Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${log.executionTimeMs}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyan,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextMuted,
                    fontSize = 9.sp
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onRerun, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Rerun",
                        tint = JarvisCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Raw user command
            Text(
                text = "“${log.rawCommand}”",
                style = MaterialTheme.typography.bodyMedium,
                color = JarvisTextPrimary,
                fontSize = 12.sp
            )

            // JARVIS Response
            if (log.jarvisReply.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "JARVIS: ${log.jarvisReply}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JarvisTextSecondary.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
