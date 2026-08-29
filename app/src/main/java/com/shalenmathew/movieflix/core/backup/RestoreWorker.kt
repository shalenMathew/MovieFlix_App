package com.shalenmathew.movieflix.core.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
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
import java.io.BufferedReader
import java.io.InputStreamReader

class RestoreWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RestoreWorkerEntryPoint {
        fun movieDatabase(): MovieDatabase
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val uriString = inputData.getString("URI") ?: return@withContext Result.failure()
        val uri = Uri.parse(uriString)

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                RestoreWorkerEntryPoint::class.java
            )
            val db = entryPoint.movieDatabase()
            val movieDao = db.dao
            val seriesDao = db.seriesTrackingDao
            val customListDao = db.customListDao

            val json = applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure()

            val backupData = Gson().fromJson(json, BackupData::class.java)

            db.withTransaction {
                // Clear existing data
                seriesDao.deleteAllEpisodes()
                seriesDao.deleteAllSeasons()
                seriesDao.deleteAllProgress()
                seriesDao.deleteAllSeries()
                seriesDao.deleteAllWatchList()
                seriesDao.deleteAllFavorites()
                seriesDao.deleteAllScheduled()
                customListDao.deleteAllCustomListMovies()
                customListDao.deleteAllLists()
                movieDao.deleteAllGalleryImages()

                // Insert backup data
                movieDao.insertFavMovies(backupData.favorites)
                movieDao.insertWatchListItems(backupData.watchList)
                movieDao.insertScheduledMovies(backupData.scheduled)
                movieDao.insertGalleryImages(backupData.galleryImages)
                
                backupData.series.forEach { seriesDao.insertSeries(it) }
                seriesDao.insertSeasons(backupData.seasons)
                seriesDao.insertEpisodes(backupData.episodes)
                backupData.progress.forEach { seriesDao.insertProgress(it) }

                customListDao.insertLists(backupData.customLists)
                customListDao.insertCustomListMovies(backupData.customListMovies)
            }

            NotificationHelper.showStatusNotification(
                applicationContext,
                "Restore Successful",
                "Your library data has been restored."
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationHelper.showStatusNotification(
                applicationContext,
                "Restore Failed",
                "Invalid backup file or corrupted data."
            )
            Result.failure()
        }
    }
}