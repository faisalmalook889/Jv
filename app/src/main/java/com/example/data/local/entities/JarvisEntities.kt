package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jarvis_memory")
data class JarvisMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val category: String = "general", // "preference", "contact", "routine", "favorite_song", "favorite_app"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jarvis_notes")
data class JarvisNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "note", // "note", "code", "email_draft", "plan", "summary"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jarvis_reminders")
data class JarvisReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val scheduledTime: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "jarvis_command_logs")
data class CommandLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawCommand: String,
    val jarvisReply: String,
    val stepCount: Int,
    val status: String, // "SUCCESS", "PARTIAL", "FAILED", "RUNNING"
    val executionTimeMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)
