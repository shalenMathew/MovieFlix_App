package com.shalenmathew.movieflix.core.backup

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.shalenmathew.movieflix.core.notifications.NotificationHelper
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BackupWorkerEntryPoint {
        fun movieDatabase(): MovieDatabase
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val uriString = inputData.getString("URI") ?: return@withContext Result.failure()
        val uri = Uri.parse(uriString)

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                BackupWorkerEntryPoint::class.java
            )
            val db = entryPoint.movieDatabase()
            val movieDao = db.dao
            val seriesDao = db.seriesTrackingDao
            val customListDao = db.customListDao

            val backupData = BackupData(
                favorites = movieDao.getAllFavMoviesSync(),
                watchList = movieDao.getAllWatchListDataSync(),
                scheduled = movieDao.getAllScheduledMoviesSync(),
                series = seriesDao.getAllSeriesSync(),
                seasons = seriesDao.getAllSeasonsSync(),
                episodes = seriesDao.getAllEpisodesSync(),
                progress = seriesDao.getAllProgressSync(),
                customLists = customListDao.getAllListsSync(),
                customListMovies = customListDao.getAllCustomListMoviesSync(),
                galleryImages = movieDao.getAllGalleryImagesSync()
            )

            val json = Gson().toJson(backupData)

            applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(json)
                }
            }

            NotificationHelper.showStatusNotification(
                applicationContext,
                "Backup Successful",
                "Your library data has been exported."
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationHelper.showStatusNotification(
                applicationContext,
                "Backup Failed",
                "Something went wrong while exporting data."
            )
            Result.failure()
        }
    }
}