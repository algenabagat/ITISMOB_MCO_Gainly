package com.itismob.s17.gainly

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Workout Reminders"
            val descriptionText = "Notifications for upcoming scheduled workouts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("WORKOUT_REMINDERS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(plan: Plan) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("WORKOUT_NAME", plan.workout.name)
            putExtra("WORKOUT_DESC", "Your scheduled workout starts in an hour.")
            putExtra("PLAN_ID", plan.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plan.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, plan.year)
            set(Calendar.MONTH, plan.month)
            set(Calendar.DAY_OF_MONTH, plan.day)
            set(Calendar.HOUR_OF_DAY, plan.hour)
            set(Calendar.MINUTE, plan.minute)
            set(Calendar.SECOND, 0)
            add(Calendar.HOUR_OF_DAY, -1)
        }

        // Only schedule if it's in the future
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    context,
                    "Permission to schedule exact alarms is required.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotification(plan: Plan) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            plan.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}