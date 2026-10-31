package com.example.taskly.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.taskly.data.model.Subject
import com.example.taskly.data.model.Task

@Database(
    entities = [Task::class, Subject::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subjectDao(): SubjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "taskly.db",
                )
                    .addCallback(SeedSubjectsCallback)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

private object SeedSubjectsCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        listOf(
            "Mobile Computing" to 0xFF4F46E5.toInt(),
            "Calculus" to 0xFFE11D48.toInt(),
            "Physics" to 0xFF0D9488.toInt(),
            "English" to 0xFFD97706.toInt(),
        ).forEach { (name, color) ->
            db.execSQL(
                "INSERT INTO subjects (name, colorArgb) VALUES (?, ?)",
                arrayOf<Any>(name, color),
            )
        }
    }
}
