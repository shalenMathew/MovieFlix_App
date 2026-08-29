package com.shalenmathew.movieflix.core.backup

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.shalenmathew.movieflix.core.notifications.NotificationHelper
import com.shalenmathew.movieflix.core.utils.ZipHelper
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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

        val backupDir = File(applicationContext.cacheDir, "backup_tmp_${System.currentTimeMillis()}")
        backupDir.mkdirs()

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

            // Save JSON to temp dir
            val jsonFile = File(backupDir, "backup.json")
            val json = Gson().toJson(backupData)
            jsonFile.writeText(json)

            // Collect all local media files
            val filesToZip = mutableListOf<File>()
            filesToZip.add(jsonFile)

            // Scan filesDir for posters/banners/gallery images
            applicationContext.filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("poster_") || 
                    file.name.startsWith("banner_") || 
                    file.name.startsWith("gallery_")) {
                    filesToZip.add(file)
                }
            }

            // Create ZIP in cache
            val zipFile = File(applicationContext.cacheDir, "temp_backup.zip")
            ZipHelper.zip(filesToZip, zipFile)

            // Write ZIP to destination URI
            applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                zipFile.inputStream().use { it.copyTo(outputStream) }
            }

            // Cleanup
            backupDir.deleteRecursively()
            zipFile.delete()

            NotificationHelper.showStatusNotification(
                applicationContext,
                "Backup Successful",
                "Your library data has been exported."
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            backupDir.deleteRecursively()
            NotificationHelper.showStatusNotification(
                applicationContext,
                "Backup Failed",
                "Something went wrong while exporting data."
            )
            Result.failure()
        }
    }
}
