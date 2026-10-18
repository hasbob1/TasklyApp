package com.example.taskly.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.taskly.R
import java.time.Instant
import java.time.ZoneId

object NotificationHelper {
    const val CHANNEL_ID = "taskly_deadlines"

    private const val BRAND_COLOR = 0xFF4F46E5.toInt()

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = context.getString(R.string.notif_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun showDeadlineNotification(
        context: Context,
        taskId: Long,
        title: String,
        subjectName: String?,
        kind: ReminderKind,
        deadlineMillis: Long,
    ) {
        if (!canPostNotifications(context)) return
        createChannel(context)

        val notificationId = taskId.toInt() * 2 + kind.ordinal
        val timeText = Instant.ofEpochMilli(deadlineMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .formatShort()
        val hasSubject = !subjectName.isNullOrBlank()

        val (notificationTitle, text) = when (kind) {
            ReminderKind.EARLY -> context.getString(R.string.notif_title_due_soon, title) to
                    if (hasSubject) context.getString(R.string.notif_text_early_subject, timeText, subjectName)
                    else context.getString(R.string.notif_text_early, timeText)
            ReminderKind.DEADLINE -> context.getString(R.string.notif_title_due_now, title) to
                    if (hasSubject) context.getString(R.string.notif_text_subject, subjectName)
                    else context.getString(R.string.notif_text_generic)
        }

        // tapping the notification opens the app
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                notificationId,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_taskly)
            .setColor(BRAND_COLOR)
            .setContentTitle(notificationTitle)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}
