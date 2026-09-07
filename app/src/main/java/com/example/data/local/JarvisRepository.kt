package com.example.data.local

import com.example.data.local.entities.CommandLogEntity
import com.example.data.local.entities.JarvisMemoryEntity
import com.example.data.local.entities.JarvisNoteEntity
import com.example.data.local.entities.JarvisReminderEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val dao: JarvisDao) {

    val allMemory: Flow<List<JarvisMemoryEntity>> = dao.getAllMemory()
    val allNotes: Flow<List<JarvisNoteEntity>> = dao.getAllNotes()
    val allReminders: Flow<List<JarvisReminderEntity>> = dao.getAllReminders()
    val recentLogs: Flow<List<CommandLogEntity>> = dao.getRecentLogs()

    suspend fun saveMemory(key: String, value: String, category: String = "general"): Long {
        return dao.insertMemory(JarvisMemoryEntity(key = key, value = value, category = category))
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteMemoryById(id)
    }

    suspend fun saveNote(title: String, content: String, category: String = "note"): Long {
        return dao.insertNote(JarvisNoteEntity(title = title, content = content, category = category))
    }

    suspend fun deleteNote(id: Long) {
        dao.deleteNoteById(id)
    }

    suspend fun saveReminder(title: String, scheduledTime: String): Long {
        return dao.insertReminder(JarvisReminderEntity(title = title, scheduledTime = scheduledTime))
    }

    suspend fun toggleReminder(reminder: JarvisReminderEntity) {
        dao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
    }

    suspend fun deleteReminder(id: Long) {
        dao.deleteReminderById(id)
    }

    suspend fun logExecution(rawCommand: String, reply: String, stepCount: Int, status: String, latencyMs: Long): Long {
        return dao.insertLog(
            CommandLogEntity(
                rawCommand = rawCommand,
                jarvisReply = reply,
                stepCount = stepCount,
                status = status,
                executionTimeMs = latencyMs
            )
        )
    }

    suspend fun clearAllLogs() {
        dao.clearLogs()
    }
}
