package com.example.taskly.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.taskly.R
import com.example.taskly.data.model.Task

object ShareUtils {
    fun buildShareText(context: Context, task: Task, subjectName: String?): String = buildString {
        appendLine(
            context.getString(
                R.string.share_body,
                task.title,
                subjectName ?: context.getString(R.string.task_card_no_subject),
                "${task.dueDate.formatFull()}, ${task.dueTime.formatShort()}",
                context.getString(task.priority.labelRes()),
            ),
        )
        if (task.description.isNotBlank()) {
            appendLine(context.getString(R.string.share_notes, task.description))
        }
        append(context.getString(R.string.share_footer))
    }

    fun shareTask(context: Context, task: Task, subjectName: String?) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, task.title)
            putExtra(Intent.EXTRA_TEXT, buildShareText(context, task, subjectName))
        }
        val chooser = Intent.createChooser(sendIntent, context.getString(R.string.share_chooser_title))
        if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
