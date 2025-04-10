package com.example.fitnesstrackerapp.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.utils.NotificationHelper

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val workoutType = intent.getStringExtra("workout_type") ?: return
        val duration = intent.getIntExtra("duration", 0)

        val notificationHelper = NotificationHelper(context)
        val notification = notificationHelper.createNotification(workoutType, duration).build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationManager.notify(
            intent.getIntExtra("notification_id", 0),
            notification
        )
    }
}