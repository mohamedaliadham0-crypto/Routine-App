package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Prayer & Study Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your scheduled routine"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "routine_study_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Study & Prayer Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for prayers and study routine tasks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}

object NotificationScheduler {
    fun schedulePrayerReminder(context: Context, prayerName: String, timeStr: String) {
        try {
            val parts = timeStr.split(":").map { it.toInt() }
            val prayerTime = LocalTime.of(parts[0], parts[1])
            val now = LocalTime.now()

            var targetDate = LocalDate.now()
            if (prayerTime.isBefore(now)) {
                targetDate = targetDate.plusDays(1)
            }

            val targetDateTime = targetDate.atTime(prayerTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("title", "Prayer Reminder: $prayerName")
                putExtra("message", "It is now time for $prayerName prayer ($timeStr)")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayerName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetDateTime, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
