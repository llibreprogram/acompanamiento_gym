package com.gymcompanion.app.presentation.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gymcompanion.app.R
import com.gymcompanion.app.presentation.MainActivity

class WorkoutNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "workout_channel"
        const val NOTIFICATION_ID = 1001
        
        const val ACTION_NEXT_EXERCISE = "com.gymcompanion.app.ACTION_NEXT_EXERCISE"
        const val ACTION_FINISH_WORKOUT = "com.gymcompanion.app.ACTION_FINISH_WORKOUT"
        
        const val EXTRA_EXERCISE_NAME = "exercise_name"
        const val EXTRA_TIMER = "timer"
        const val EXTRA_ROUTINE_ID = "routine_id"

        fun startService(context: Context, routineId: Long, exerciseName: String, timer: String) {
            val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                putExtra(EXTRA_ROUTINE_ID, routineId)
                putExtra(EXTRA_EXERCISE_NAME, exerciseName)
                putExtra(EXTRA_TIMER, timer)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkoutNotificationService::class.java)
            context.stopService(intent)
        }

        fun updateNotification(context: Context, exerciseName: String, timer: String) {
            val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                putExtra(EXTRA_EXERCISE_NAME, exerciseName)
                putExtra(EXTRA_TIMER, timer)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private var currentExerciseName: String = "Workout"
    private var currentTimer: String = "00:00"
    private var routineId: Long = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            routineId = it.getLongExtra(EXTRA_ROUTINE_ID, 0)
            currentExerciseName = it.getStringExtra(EXTRA_EXERCISE_NAME) ?: currentExerciseName
            currentTimer = it.getStringExtra(EXTRA_TIMER) ?: currentTimer
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Workout Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing workout session notifications"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Intent to open app
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Exercise action
        val nextExerciseIntent = Intent(this, WorkoutNotificationReceiver::class.java).apply {
            action = ACTION_NEXT_EXERCISE
        }
        val nextExercisePendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            nextExerciseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Finish Workout action
        val finishWorkoutIntent = Intent(this, WorkoutNotificationReceiver::class.java).apply {
            action = ACTION_FINISH_WORKOUT
        }
        val finishWorkoutPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            finishWorkoutIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🏋️ Workout en Progreso")
            .setContentText(currentExerciseName)
            .setSubText("⏱️ $currentTimer")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Siguiente",
                nextExercisePendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Finalizar",
                finishWorkoutPendingIntent
            )
            .build()
    }
}
