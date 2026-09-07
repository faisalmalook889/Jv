package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.entities.CommandLogEntity
import com.example.data.local.entities.JarvisMemoryEntity
import com.example.data.local.entities.JarvisNoteEntity
import com.example.data.local.entities.JarvisReminderEntity

@Database(
    entities = [
        JarvisMemoryEntity::class,
        JarvisNoteEntity::class,
        JarvisReminderEntity::class,
        CommandLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_master.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
