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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ProductivityMemoryScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val memoryList by viewModel.memoryList.collectAsState()
    val notesList by viewModel.notesList.collectAsState()
    val remindersList by viewModel.remindersList.collectAsState()

    var newMemoryKey by remember { mutableStateOf("") }
    var newMemoryVal by remember { mutableStateOf("") }

    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteBody by remember { mutableStateOf("") }

    var newReminderTitle by remember { mutableStateOf("") }
    var newReminderTime by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Selector Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = JarvisSurfaceElevated,
            contentColor = JarvisCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                    color = JarvisCyan
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, JarvisCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("SMART MEMORY", fontSize = 11.sp, style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("NOTES & TASKS", fontSize = 11.sp, style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("AI WORKFLOWS", fontSize = 11.sp, style = MaterialTheme.typography.labelSmall) }
            )
        }

        when (selectedSection) {
            0 -> {
                // Smart Memory Bank
                HudCard(
                    title = "Memory Matrix (${memoryList.size} Facts Stored)",
                    icon = Icons.Default.Psychology,
                    badge = "ACTIVE",
                    borderColor = JarvisCyan,
                    modifier = Modifier.testTag("smart_memory_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Add Memory Form
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = newMemoryKey,
                                onValueChange = { newMemoryKey = it },
                                placeholder = { Text("Key (e.g. Favorite Song, Contact Alex)", fontSize = 11.sp, color = JarvisTextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisCyan.copy(alpha = 0.3f),
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    focusedContainerColor = JarvisSurfaceElevated,
                                    unfocusedContainerColor = JarvisSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = newMemoryVal,
                                onValueChange = { newMemoryVal = it },
                                placeholder = { Text("Value (e.g. Starboy, +123456)", fontSize = 11.sp, color = JarvisTextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisCyan,
                                    unfocusedBorderColor = JarvisCyan.copy(alpha = 0.3f),
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    focusedContainerColor = JarvisSurfaceElevated,
                                    unfocusedContainerColor = JarvisSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                if (newMemoryKey.isNotBlank() && newMemoryVal.isNotBlank()) {
                                    viewModel.addMemory(newMemoryKey, newMemoryVal)
                                    newMemoryKey = ""
                                    newMemoryVal = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_memory_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("COMMIT TO MEMORY BANK", style = MaterialTheme.typography.labelSmall)
                        }

                        // Memory List
                        if (memoryList.isEmpty()) {
                            Text(
                                text = "No personal memory entries yet. JARVIS will automatically learn from your voice commands or you can add them manually above.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JarvisTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            memoryList.forEach { memory ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JarvisSurfaceElevated)
                                        .border(0.5.dp, JarvisCyan.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = memory.key.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JarvisCyan,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = memory.value,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = JarvisTextPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteMemory(memory.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = JarvisRed.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Notes & Reminders
                HudCard(
                    title = "Reminders & Scheduled Tasks (${remindersList.size})",
                    icon = Icons.Default.Notifications,
                    badge = "ACTIVE",
                    borderColor = JarvisGreen,
                    modifier = Modifier.testTag("reminders_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = newReminderTitle,
                                onValueChange = { newReminderTitle = it },
                                placeholder = { Text("Task (e.g. Call Client)", fontSize = 11.sp, color = JarvisTextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisGreen,
                                    unfocusedBorderColor = JarvisGreen.copy(alpha = 0.3f),
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    focusedContainerColor = JarvisSurfaceElevated,
                                    unfocusedContainerColor = JarvisSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = newReminderTime,
                                onValueChange = { newReminderTime = it },
                                placeholder = { Text("Time (e.g. 4 PM)", fontSize = 11.sp, color = JarvisTextMuted) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JarvisGreen,
                                    unfocusedBorderColor = JarvisGreen.copy(alpha = 0.3f),
                                    focusedTextColor = JarvisTextPrimary,
                                    unfocusedTextColor = JarvisTextPrimary,
                                    focusedContainerColor = JarvisSurfaceElevated,
                                    unfocusedContainerColor = JarvisSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        Button(
                            onClick = {
                                if (newReminderTitle.isNotBlank()) {
                                    viewModel.addReminder(newReminderTitle, newReminderTime.ifEmpty { "Today" })
                                    newReminderTitle = ""
                                    newReminderTime = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ADD REMINDER PROTOCOL", style = MaterialTheme.typography.labelSmall)
                        }

                        remindersList.forEach { rem ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisSurfaceElevated)
                                    .clickable { viewModel.toggleReminder(rem) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (rem.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (rem.isCompleted) JarvisGreen else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rem.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (rem.isCompleted) JarvisTextMuted else JarvisTextPrimary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = rem.scheduledTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JarvisGreen,
                                        fontSize = 9.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteReminder(rem.id) },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisRed.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Saved Notes / Code Snippets
                HudCard(
                    title = "Saved Documents & Notes (${notesList.size})",
                    icon = Icons.AutoMirrored.Filled.Note,
                    badge = "ROOM DB",
                    borderColor = JarvisAmber,
                    modifier = Modifier.testTag("saved_notes_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        notesList.forEach { note ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisSurfaceElevated)
                                    .border(0.5.dp, JarvisAmber.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = JarvisAmber,
                                            fontSize = 12.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.deleteNote(note.id) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = JarvisRed.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = JarvisTextPrimary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // AI Workflow Launchers
                HudCard(
                    title = "AI Automation Workflows",
                    icon = Icons.Default.Code,
                    badge = "PRODUCTIVITY",
                    borderColor = JarvisCyan,
                    modifier = Modifier.testTag("ai_workflows_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WorkflowTriggerRow(
                            icon = Icons.Default.Email,
                            title = "Executive Email Generator",
                            subtitle = "Draft polished professional emails and subject lines",
                            color = JarvisCyan,
                            onClick = { viewModel.executeVoiceCommand("Write a professional email to client proposing project milestone deliverables") }
                        )

                        WorkflowTriggerRow(
                            icon = Icons.Default.Code,
                            title = "Code & Script Architect",
                            subtitle = "Generate, debug, or optimize algorithms and scripts",
                            color = JarvisGreen,
                            onClick = { viewModel.executeVoiceCommand("Generate Kotlin code for parsing JSON with Moshi and handling network errors") }
                        )

                        WorkflowTriggerRow(
                            icon = Icons.Default.Functions,
                            title = "Mathematical Engine",
                            subtitle = "Solve complex calculus, arithmetic, and STEM problems",
                            color = JarvisAmber,
                            onClick = { viewModel.executeVoiceCommand("Solve 250 * 18.5 - 450 / 3 step by step") }
                        )

                        WorkflowTriggerRow(
                            icon = Icons.Default.Flight,
                            title = "Travel & Expedition Planner",
                            subtitle = "Generate itinerary, budget, and travel schedule",
                            color = JarvisCyan,
                            onClick = { viewModel.executeVoiceCommand("Create a 3-day travel plan for Tokyo with top highlights") }
                        )

                        WorkflowTriggerRow(
                            icon = Icons.Default.FitnessCenter,
                            title = "Workout & Diet Protocol",
                            subtitle = "Structured physical training and nutrition roadmap",
                            color = JarvisGreen,
                            onClick = { viewModel.executeVoiceCommand("Generate a high protein weekly workout and diet routine") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WorkflowTriggerRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisSurfaceElevated)
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = JarvisTextPrimary, fontSize = 12.sp)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = JarvisTextMuted, fontSize = 10.sp)
        }
        Text("RUN", style = MaterialTheme.typography.labelSmall, color = color, fontSize = 10.sp)
    }
}
