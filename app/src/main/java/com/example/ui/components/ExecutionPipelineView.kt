package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JarvisStepItem
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ExecutionPipelineView(
    steps: List<JarvisStepItem>,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    HudCard(
        title = "Execution Pipeline (${steps.size} Sub-Tasks)",
        icon = Icons.Default.PlayArrow,
        badge = "AUTO",
        borderColor = JarvisCyan,
        modifier = modifier.testTag("execution_pipeline_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.forEach { step ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    StepItemRow(step = step)
                }
            }
        }
    }
}

@Composable
private fun StepItemRow(step: JarvisStepItem) {
    val infiniteTransition = rememberInfiniteTransition(label = "step_spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val (statusColor, statusIcon) = when (step.status) {
        "COMPLETED" -> Pair(JarvisGreen, Icons.Default.CheckCircle)
        "RUNNING" -> Pair(JarvisAmber, Icons.Default.Sync)
        "FAILED" -> Pair(JarvisRed, Icons.Default.Error)
        else -> Pair(Color.Gray, Icons.Default.HourglassEmpty)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(JarvisSurfaceElevated.copy(alpha = 0.6f))
            .border(0.5.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("step_item_${step.stepNumber}")
    ) {
        // Step Number Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .background(statusColor.copy(alpha = 0.2f), CircleShape)
        ) {
            Text(
                text = "${step.stepNumber}",
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Step description & action tag
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                color = JarvisTextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = "ACTION: ${step.actionType}" + if (step.target.isNotBlank()) " [${step.target}]" else "",
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextSecondary.copy(alpha = 0.7f),
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Status Icon / Spinner
        if (step.status == "RUNNING") {
            Icon(
                imageVector = statusIcon,
                contentDescription = step.status,
                tint = statusColor,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
            )
        } else {
            Icon(
                imageVector = statusIcon,
                contentDescription = step.status,
                tint = statusColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
