package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder

@Composable
fun HudCard(
    title: String? = null,
    icon: ImageVector? = null,
    badge: String? = null,
    borderColor: Color = JarvisCyan,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(JarvisSurface.copy(alpha = 0.85f))
            .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(12.dp))
    ) {
        // Futuristic Corner Accents
        Canvas(modifier = Modifier.matchParentSize()) {
            val bracketLen = 14.dp.toPx()
            val strokeW = 2.dp.toPx()

            // Top-left bracket
            drawLine(borderColor, Offset(0f, 0f), Offset(bracketLen, 0f), strokeW)
            drawLine(borderColor, Offset(0f, 0f), Offset(0f, bracketLen), strokeW)

            // Top-right bracket
            drawLine(borderColor, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), strokeW)
            drawLine(borderColor, Offset(size.width, 0f), Offset(size.width, bracketLen), strokeW)

            // Bottom-left bracket
            drawLine(borderColor, Offset(0f, size.height), Offset(bracketLen, size.height), strokeW)
            drawLine(borderColor, Offset(0f, size.height), Offset(0f, size.height - bracketLen), strokeW)

            // Bottom-right bracket
            drawLine(borderColor, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), strokeW)
            drawLine(borderColor, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), strokeW)
        }

        Column(modifier = Modifier.padding(14.dp)) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = borderColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = borderColor,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(borderColor.copy(alpha = 0.15f))
                                .border(0.5.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = borderColor,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}
