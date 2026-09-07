package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurfaceElevated

data class QuickPrompt(
    val title: String,
    val command: String,
    val icon: ImageVector,
    val accentColor: androidx.compose.ui.graphics.Color = JarvisCyan
)

@Composable
fun QuickActionChips(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val samplePrompts = listOf(
        QuickPrompt("Multi-Task Flow", "Open YouTube, search Arijit Singh songs, play the latest song and increase volume to 70%", Icons.Default.Bolt, JarvisAmber),
        QuickPrompt("WhatsApp Message", "Send WhatsApp message to +1234567890 saying Meeting in 10 minutes", Icons.AutoMirrored.Filled.Message, JarvisGreen),
        QuickPrompt("Volume 80%", "Set volume to 80%", Icons.AutoMirrored.Filled.VolumeUp, JarvisCyan),
        QuickPrompt("Torch Protocol", "Toggle flashlight", Icons.Default.FlashlightOn, JarvisAmber),
        QuickPrompt("Compose Email", "Write a professional email to manager about weekly sprint update", Icons.Default.Email, JarvisCyan),
        QuickPrompt("Play Trending", "Play top trending songs on YouTube", Icons.Default.PlayArrow, JarvisCyan)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp)
            .testTag("quick_action_chips_row")
    ) {
        samplePrompts.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(JarvisSurfaceElevated.copy(alpha = 0.8f))
                    .border(1.dp, item.accentColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { onSelectPrompt(item.command) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("quick_chip_$index")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = item.accentColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
