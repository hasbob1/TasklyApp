package com.example.taskly.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.taskly.data.model.Task
import java.time.ZoneId

enum class ReminderKind { EARLY, DEADLINE }

interface ReminderScheduler {
    fun schedule(task: Task, subjectName: String? = null)
    fun cancel(taskId: Long)
}

class AlarmReminderScheduler(context: Context) : ReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    override fun schedule(task: Task, subjectName: String?) {
        cancel(task.id)
        if (task.isCompleted) return

        val deadlineMillis = task.dueDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        setAlarm(task, subjectName, ReminderKind.DEADLINE, deadlineMillis, deadlineMillis)
        if (task.remindBeforeMinutes > 0) {
            val earlyMillis = deadlineMillis - task.remindBeforeMinutes * MILLIS_PER_MINUTE
            setAlarm(task, subjectName, ReminderKind.EARLY, earlyMillis, deadlineMillis)
        }
    }

    override fun cancel(taskId: Long) {
        ReminderKind.entries.forEach { kind ->
            val existing = PendingIntent.getBroadcast(
                appContext,
                requestCode(taskId, kind),
                Intent(appContext, DeadlineReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (existing != null) {
                alarmManager.cancel(existing)
                existing.cancel()
            }
        }
    }

    private fun setAlarm(task: Task, subjectName: String?, kind: ReminderKind, triggerAt: Long, deadlineMillis: Long) {
        if (triggerAt <= System.currentTimeMillis()) return

        val intent = Intent(appContext, DeadlineReceiver::class.java).apply {
            putExtra(DeadlineReceiver.EXTRA_TASK_ID, task.id)
            putExtra(DeadlineReceiver.EXTRA_TITLE, task.title)
            putExtra(DeadlineReceiver.EXTRA_SUBJECT, subjectName)
            putExtra(DeadlineReceiver.EXTRA_KIND, kind.name)
            putExtra(DeadlineReceiver.EXTRA_DEADLINE_MILLIS, deadlineMillis)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            requestCode(task.id, kind),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            if (canBeExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (_: SecurityException) {

            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun requestCode(taskId: Long, kind: ReminderKind): Int = taskId.toInt() * 2 + kind.ordinal

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
