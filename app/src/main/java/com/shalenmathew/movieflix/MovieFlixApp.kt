package com.shalenmathew.movieflix

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.impl.WorkManagerImpl
import com.shalenmathew.movieflix.core.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MovieFlixApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            Log.d("MovieFlixApp", "Providing WorkManager Configuration")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        Log.d("MovieFlixApp", "onCreate")

        // Explicitly initialize WorkManager to ensure HiltWorkerFactory is used.
        try {
            WorkManager.initialize(this, workManagerConfiguration)
            Log.d("MovieFlixApp", "WorkManager initialized explicitly")
        } catch (e: Exception) {
            Log.e("MovieFlixApp", "WorkManager already initialized: ${e.message}")
        }
        
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}
