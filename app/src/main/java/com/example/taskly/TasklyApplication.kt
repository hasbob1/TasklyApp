package com.example.taskly

import android.app.Application
import com.example.taskly.data.InMemoryTaskRepository
import com.example.taskly.data.TaskRepository
import com.example.taskly.util.AlarmReminderScheduler
import com.example.taskly.util.ReminderScheduler

class TasklyApplication : Application() {
    val repository: TaskRepository by lazy { InMemoryTaskRepository() }

    val reminderScheduler: ReminderScheduler by lazy { AlarmReminderScheduler(this) }
}
