package com.itismob.s17.gainly

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val workoutName = intent.getStringExtra("WORKOUT_NAME") ?: "Workout"
        val workoutDescription =
            intent.getStringExtra("WORKOUT_DESC") ?: "It's time to start your workout!"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "WORKOUT_REMINDERS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$workoutName is starting soon!")
            .setContentText(workoutDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationId = intent.getIntExtra("PLAN_ID", System.currentTimeMillis().toInt())
        notificationManager.notify(notificationId, notification)
    }
}