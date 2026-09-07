package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun JarvisConfirmationDialog(
    prompt: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(JarvisBackground)
                .border(1.5.dp, JarvisRed, RoundedCornerShape(16.dp))
                .padding(20.dp)
                .testTag("security_confirmation_dialog")
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Warning Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Alert",
                        tint = JarvisRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SECURITY PROTOCOL 4",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisRed,
                        letterSpacing = 1.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = JarvisTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_action_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisCyan.copy(alpha = 0.5f))
                    ) {
                        Text("ABORT", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_action_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisRed,
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        Text("AUTHORIZE", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
