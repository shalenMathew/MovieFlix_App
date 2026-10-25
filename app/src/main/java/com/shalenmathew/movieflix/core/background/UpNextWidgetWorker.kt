package com.shalenmathew.movieflix.core.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.shalenmathew.movieflix.presentation.widget.updateUpNextWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UpNextWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            updateUpNextWidget(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "UpNextWidgetPeriodicSync"

        fun schedulePeriodicSync(context: Context) {
            try {
                val workManager = WorkManager.getInstance(context)
                val syncRequest = PeriodicWorkRequestBuilder<UpNextWidgetWorker>(
                    6, TimeUnit.HOURS
                ).build()

                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}