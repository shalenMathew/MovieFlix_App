package com.shalenmathew.movieflix

import android.app.Application
import com.shalenmathew.movieflix.core.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovieFlixApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)
    }
}