package com.example.taskly.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeadlineReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val kind = intent.getStringExtra(EXTRA_KIND)
            ?.let { name -> ReminderKind.entries.firstOrNull { it.name == name } }
            ?: ReminderKind.DEADLINE
        NotificationHelper.showDeadlineNotification(
            context = context,
            taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L),
            title = title,
            subjectName = intent.getStringExtra(EXTRA_SUBJECT),
            kind = kind,
            deadlineMillis = intent.getLongExtra(EXTRA_DEADLINE_MILLIS, 0L),
        )
    }

    companion object {
        const val EXTRA_TASK_ID = "taskly.extra.TASK_ID"
        const val EXTRA_TITLE = "taskly.extra.TITLE"
        const val EXTRA_SUBJECT = "taskly.extra.SUBJECT"
        const val EXTRA_KIND = "taskly.extra.KIND"
        const val EXTRA_DEADLINE_MILLIS = "taskly.extra.DEADLINE_MILLIS"
    }
}
