package com.gymcompanion.app.presentation.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WorkoutNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WorkoutNotificationService.ACTION_NEXT_EXERCISE -> {
                // Send broadcast to WorkoutViewModel to advance exercise
                val broadcastIntent = Intent("com.gymcompanion.app.NEXT_EXERCISE")
                context.sendBroadcast(broadcastIntent)
            }
            WorkoutNotificationService.ACTION_FINISH_WORKOUT -> {
                // Send broadcast to WorkoutViewModel to finish workout
                val broadcastIntent = Intent("com.gymcompanion.app.FINISH_WORKOUT")
                context.sendBroadcast(broadcastIntent)
                
                // Stop the notification service
                WorkoutNotificationService.stopService(context)
            }
        }
    }
}
