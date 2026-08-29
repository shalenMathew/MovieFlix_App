package com.shalenmathew.movieflix.core.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
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

        val restoreDir = File(applicationContext.cacheDir, "restore_tmp_${System.currentTimeMillis()}")
        restoreDir.mkdirs()

        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                RestoreWorkerEntryPoint::class.java
            )
            val db = entryPoint.movieDatabase()
            val movieDao = db.dao
            val seriesDao = db.seriesTrackingDao
            val customListDao = db.customListDao

            // Copy ZIP to cache
            val tempZipFile = File(applicationContext.cacheDir, "restore_data.zip")
            applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                tempZipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Unzip
            ZipHelper.unzip(tempZipFile, restoreDir)

            // Read JSON
            val jsonFile = File(restoreDir, "backup.json")
            if (!jsonFile.exists()) return@withContext Result.failure()
            val json = jsonFile.readText()
            val backupData = Gson().fromJson(json, BackupData::class.java)

            // Restore images to filesDir and update database paths
            restoreDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("poster_") || 
                    file.name.startsWith("banner_") || 
                    file.name.startsWith("gallery_")) {
                    file.copyTo(File(applicationContext.filesDir, file.name), overwrite = true)
                }
            }

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

                // Process and Insert backup data with updated local paths
                val updatedFavorites = backupData.favorites.map { fav ->
                    fav.copy(movieResult = fav.movieResult.copy(
                        posterPath = updatePath(fav.movieResult.posterPath),
                        backdropPath = updatePath(fav.movieResult.backdropPath)
                    ))
                }
                movieDao.insertFavMovies(updatedFavorites)

                val updatedWatchList = backupData.watchList.map { item ->
                    item.copy(movieResult = item.movieResult.copy(
                        posterPath = updatePath(item.movieResult.posterPath),
                        backdropPath = updatePath(item.movieResult.backdropPath)
                    ))
                }
                movieDao.insertWatchListItems(updatedWatchList)

                movieDao.insertScheduledMovies(backupData.scheduled)

                val updatedGallery = backupData.galleryImages.map { img ->
                    img.copy(imagePath = updatePath(img.imagePath) ?: img.imagePath)
                }
                movieDao.insertGalleryImages(updatedGallery)
                
                backupData.series.forEach { series ->
                    seriesDao.insertSeries(series.copy(
                        posterPath = updatePath(series.posterPath),
                        backdropPath = updatePath(series.backdropPath)
                    ))
                }
                
                seriesDao.insertSeasons(backupData.seasons.map { season ->
                    season.copy(posterPath = updatePath(season.posterPath))
                })
                
                seriesDao.insertEpisodes(backupData.episodes.map { ep ->
                    ep.copy(stillPath = updatePath(ep.stillPath))
                })
                
                backupData.progress.forEach { seriesDao.insertProgress(it) }

                customListDao.insertLists(backupData.customLists)
                
                customListDao.insertCustomListMovies(backupData.customListMovies.map { movie ->
                    movie.copy(
                        posterPath = updatePath(movie.posterPath),
                        backdropPath = updatePath(movie.backdropPath)
                    )
                })
            }

            // Cleanup
            restoreDir.deleteRecursively()
            tempZipFile.delete()

            NotificationHelper.showStatusNotification(
                applicationContext,
                "Restore Successful",
                "Your library data has been restored."
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            restoreDir.deleteRecursively()
            NotificationHelper.showStatusNotification(
                applicationContext,
                "Restore Failed",
                "Invalid backup file or corrupted data."
            )
            Result.failure()
        }
    }

    private fun updatePath(oldPath: String?): String? {
        if (oldPath == null) return null
        val isLocal = oldPath.startsWith("content://") || oldPath.count { it == '/' } > 1
        return if (isLocal) {
            val fileName = oldPath.substringAfterLast('/')
            File(applicationContext.filesDir, fileName).absolutePath
        } else {
            oldPath
        }
    }
}
