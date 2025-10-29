package com.example.movieflix.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.movieflix.R
import com.example.movieflix.presentation.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "scheduled_movies_channel"
    const val CHANNEL_NAME = "Scheduled Movies"
    const val CHANNEL_DESCRIPTION = "Notifications for scheduled movies and TV shows"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showScheduledMovieNotification(
        context: Context,
        movieId: Int,
        movieTitle: String,
        movieOverview: String?,
        movieResultJson: String
    ) {
        createNotificationChannel(context)

        // Create intent to open the specific movie
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = Intent.ACTION_VIEW
            putExtra("OPEN_MOVIE_DETAILS", true)
            putExtra("MOVIE_DATA", movieResultJson)
            putExtra("MOVIE_ID", movieId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            movieId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_calendar_month_24)
            .setContentTitle("⏰ Time to watch: $movieTitle")
            .setContentText(movieOverview ?: "Your scheduled movie/show is ready to watch!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(movieOverview ?: "Your scheduled movie/show is ready to watch! Open the app to start watching.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(movieId, notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(movieId, notification)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for older Android versions
        }
    }
}
