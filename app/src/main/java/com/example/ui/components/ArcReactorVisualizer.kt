package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    rmsDb: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

    // Continuous rotation for outer rings
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 3000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Counter rotation for inner ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    val activeGlowColor = when {
        isProcessing -> JarvisAmber
        isListening -> JarvisGreen
        isSpeaking -> JarvisCyan
        else -> JarvisCyan
    }

    val statusText = when {
        isListening -> "LISTENING..."
        isProcessing -> "ANALYZING..."
        isSpeaking -> "TRANSMITTING..."
        else -> "JARVIS ONLINE"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("arc_reactor_container")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = activeGlowColor),
                    onClick = onClick
                )
                .testTag("arc_reactor_clickable")
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2

                // 1. Outer Glow Circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            activeGlowColor.copy(alpha = if (isListening || isSpeaking) 0.35f else 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )

                // 2. Outer Segmented Ring
                rotate(outerRotation, pivot = center) {
                    val outerSegments = 12
                    val sweepAngle = 20f
                    for (i in 0 until outerSegments) {
                        val startAngle = i * (360f / outerSegments)
                        drawArc(
                            color = activeGlowColor.copy(alpha = 0.7f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Outer dot ticks
                    for (i in 0 until 24) {
                        val angle = Math.toRadians((i * 15.0))
                        val tickRadius = radius * 0.92f
                        val tickPos = Offset(
                            (center.x + tickRadius * cos(angle)).toFloat(),
                            (center.y + tickRadius * sin(angle)).toFloat()
                        )
                        drawCircle(
                            color = activeGlowColor.copy(alpha = 0.5f),
                            radius = 1.5.dp.toPx(),
                            center = tickPos
                        )
                    }
                }

                // 3. Middle Ring with Dashed Line
                rotate(innerRotation, pivot = center) {
                    val middleRadius = radius * 0.75f
                    drawCircle(
                        color = activeGlowColor.copy(alpha = 0.4f),
                        radius = middleRadius,
                        center = center,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                        )
                    )

                    // Triangle/chevron markers on middle ring
                    val chevrons = 6
                    for (i in 0 until chevrons) {
                        val angle = Math.toRadians((i * (360.0 / chevrons)))
                        val pX = (center.x + middleRadius * cos(angle)).toFloat()
                        val pY = (center.y + middleRadius * sin(angle)).toFloat()
                        drawCircle(
                            color = activeGlowColor,
                            radius = 3.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                    }
                }

                // 4. Reactive Inner Core
                val rmsScale = (rmsDb / 15f).coerceIn(0f, 1f)
                val coreRadius = (radius * 0.45f) * (if (isListening || isSpeaking) (1f + rmsScale * 0.35f) else breathingPulse)

                // Glowing Core Gradient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            activeGlowColor,
                            activeGlowColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = center
                )

                // High-tech Arc Core Border
                drawCircle(
                    color = activeGlowColor,
                    radius = coreRadius * 0.8f,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
            }

            // Center Icon Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF070D1A).copy(alpha = 0.85f), CircleShape)
                    .shadow(elevation = 8.dp, shape = CircleShape)
            ) {
                Icon(
                    imageVector = when {
                        isListening -> Icons.Default.Mic
                        isSpeaking -> Icons.Default.GraphicEq
                        isProcessing -> Icons.Default.SmartToy
                        else -> Icons.Default.Mic
                    },
                    contentDescription = statusText,
                    tint = activeGlowColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Futuristic Status Label
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelLarge,
            color = activeGlowColor,
            fontSize = 12.sp,
            letterSpacing = 2.sp
        )
    }
}
