package com.shalenmathew.movieflix.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.shalenmathew.movieflix.core.background.UpNextWidgetWorker

class UpNextWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UpNextWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        UpNextWidgetWorker.schedulePeriodicSync(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        UpNextWidgetWorker.schedulePeriodicSync(context)
    }
}