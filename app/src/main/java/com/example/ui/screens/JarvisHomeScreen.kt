package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JarvisViewModel
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.ExecutionPipelineView
import com.example.ui.components.HudCard
import com.example.ui.components.QuickActionChips
import com.example.ui.components.TelemetryBar
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun JarvisHomeScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val liveRmsDb by viewModel.liveRmsDb.collectAsState()
    val partialTranscript by viewModel.partialTranscript.collectAsState()
    val conversationHistory by viewModel.conversationHistory.collectAsState()
    val activeSteps by viewModel.activeSteps.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val lastAssistantMessage = conversationHistory.lastOrNull { it.first == "JARVIS" }?.second
        ?: "Standing by for voice instruction."

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Telemetry Bar
        TelemetryBar(
            telemetry = telemetry,
            onToggleTorch = { viewModel.toggleTorchDirect() }
        )

        // Arc Reactor Visualizer (Voice Input Core)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            ArcReactorVisualizer(
                isListening = isListening,
                isSpeaking = isSpeaking,
                isProcessing = isProcessing,
                rmsDb = liveRmsDb,
                onClick = { viewModel.toggleVoiceListening() }
            )
        }

        // Live Voice Transcript Alert
        AnimatedVisibility(visible = isListening && partialTranscript.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisGreen.copy(alpha = 0.15f))
                    .border(1.dp, JarvisGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .testTag("live_transcript_box")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = JarvisGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"$partialTranscript\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisGreen,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Active Assistant Response HUD Card
        HudCard(
            title = "Neural Audio Response",
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            badge = if (isSpeaking) "SPEAKING" else "READY",
            borderColor = if (isSpeaking) JarvisCyan else JarvisCyan.copy(alpha = 0.7f),
            modifier = Modifier.testTag("assistant_response_card")
        ) {
            Text(
                text = lastAssistantMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = JarvisTextPrimary,
                fontFamily = FontFamily.Default,
                lineHeight = 22.sp
            )
        }

        // Multi-Step Execution Pipeline
        ExecutionPipelineView(steps = activeSteps)

        // Quick Action Chips
        Column {
            Text(
                text = "VOICE PROTOCOL PRESETS",
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            QuickActionChips(onSelectPrompt = { cmd -> viewModel.executeVoiceCommand(cmd) })
        }

        // Hybrid Text / Voice Command Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = "Speak or enter command...",
                        color = JarvisTextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisCyan.copy(alpha = 0.35f),
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisSurfaceElevated,
                    unfocusedContainerColor = JarvisSurfaceElevated
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Mic / Send Button
            if (textInput.isNotBlank()) {
                IconButton(
                    onClick = {
                        val cmd = textInput
                        textInput = ""
                        viewModel.executeVoiceCommand(cmd)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(JarvisCyan, CircleShape)
                        .testTag("send_command_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { viewModel.toggleVoiceListening() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (isListening) JarvisGreen else JarvisCyan.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, if (isListening) JarvisGreen else JarvisCyan, CircleShape)
                        .testTag("bottom_mic_btn")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Mic",
                        tint = if (isListening) Color.Black else JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
