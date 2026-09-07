package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.CommandLogEntity
import com.example.data.local.entities.JarvisMemoryEntity
import com.example.data.local.entities.JarvisNoteEntity
import com.example.data.local.entities.JarvisReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {
    // Smart Memory
    @Query("SELECT * FROM jarvis_memory ORDER BY timestamp DESC")
    fun getAllMemory(): Flow<List<JarvisMemoryEntity>>

    @Query("SELECT * FROM jarvis_memory WHERE category = :category ORDER BY timestamp DESC")
    fun getMemoryByCategory(category: String): Flow<List<JarvisMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: JarvisMemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: JarvisMemoryEntity)

    @Query("DELETE FROM jarvis_memory WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    // Notes / Documents / Code
    @Query("SELECT * FROM jarvis_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<JarvisNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: JarvisNoteEntity): Long

    @Delete
    suspend fun deleteNote(note: JarvisNoteEntity)

    @Query("DELETE FROM jarvis_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // Reminders
    @Query("SELECT * FROM jarvis_reminders ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllReminders(): Flow<List<JarvisReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: JarvisReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: JarvisReminderEntity)

    @Query("DELETE FROM jarvis_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    // Command Logs
    @Query("SELECT * FROM jarvis_command_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogs(): Flow<List<CommandLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CommandLogEntity): Long

    @Query("DELETE FROM jarvis_command_logs")
    suspend fun clearLogs()
}
