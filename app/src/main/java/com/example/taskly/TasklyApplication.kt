package com.example.taskly

import android.app.Application
import com.example.taskly.data.RoomTaskRepository
import com.example.taskly.data.TaskRepository
import com.example.taskly.data.local.AppDatabase
import com.example.taskly.util.AlarmReminderScheduler
import com.example.taskly.util.ReminderScheduler

class TasklyApplication : Application() {
    private val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val repository: TaskRepository by lazy {
        RoomTaskRepository(database.taskDao(), database.subjectDao())
    }

    val reminderScheduler: ReminderScheduler by lazy { AlarmReminderScheduler(this) }
}
