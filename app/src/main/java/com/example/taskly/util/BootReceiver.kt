package com.example.taskly.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.taskly.TasklyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        val app = context.applicationContext as TasklyApplication
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val subjects = app.repository.observeSubjects().first().associateBy { it.id }
                app.repository.observeTasks().first()
                    .filterNot { it.isCompleted }
                    .forEach { task ->
                        runCatching {
                            app.reminderScheduler.schedule(task, subjects[task.subjectId]?.name)
                        }.onFailure { Log.e(TAG, "Failed to restore reminder for task ${task.id}", it) }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error while restoring task reminders", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "TasklyBootReceiver"
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}