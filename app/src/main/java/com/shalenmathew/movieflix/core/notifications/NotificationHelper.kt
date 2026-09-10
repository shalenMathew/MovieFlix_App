package com.shalenmathew.movieflix.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.presentation.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

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

    suspend fun showScheduledMovieNotification(
        context: Context,
        movieId: Int,
        movieTitle: String,
        moviePosterUrl: String?,
        movieResultJson: String,
        customMessage: String? = null
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


        val appLogoBitmap = getScaledBitmapFromResource(context, R.drawable.logo_5)
        

        val posterBitmap = moviePosterUrl?.let { loadImageFromUrl(it) }

        val notificationTitle = customMessage ?: "MovieFlix Reminder"
        val notificationContent = "Reminder: $movieTitle"

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setLargeIcon(appLogoBitmap) // Set app logo as default large icon

        // Add large image if poster loaded successfully
        if (posterBitmap != null) {
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(posterBitmap)
                    .setSummaryText(notificationContent)
                    .bigLargeIcon(appLogoBitmap) // Show app logo when expanded
            )
            notificationBuilder.setLargeIcon(posterBitmap) // Swap logo for poster in collapsed view
        } else {
            // Fallback to text style if image fails to load
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationContent)
            )
        }

        val notification = notificationBuilder.build()

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

    private suspend fun loadImageFromUrl(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection()
                connection.doInput = true
                connection.connect()
                val input = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
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

    fun showStatusNotification(context: Context, title: String, message: String) {
        val notificationId = 1001
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
            }
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    private fun getScaledBitmapFromResource(context: Context, resId: Int): Bitmap? {
        val resources = context.resources
        
        // Get the system's preferred large icon size (usually square)
        val targetWidth = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
        val targetHeight = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
        
        // Decode with bounds only to get original dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(resources, resId, options)
        
        // Calculate the optimal sample size
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
        options.inJustDecodeBounds = false
        
        val decodedBitmap = BitmapFactory.decodeResource(resources, resId, options) ?: return null
        
        // Create a new bitmap with the exact system dimensions and a transparent background
        val outputBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        
        // Calculate scaling to fit center without stretching (preserve aspect ratio)
        val scale = Math.min(
            targetWidth.toFloat() / decodedBitmap.width,
            targetHeight.toFloat() / decodedBitmap.height
        )
        
        val xTranslation = (targetWidth - decodedBitmap.width * scale) / 2.0f
        val yTranslation = (targetHeight - decodedBitmap.height * scale) / 2.0f
        
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(xTranslation, yTranslation)
        }
        
        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        
        canvas.drawBitmap(decodedBitmap, matrix, paint)
        
        return outputBitmap
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
