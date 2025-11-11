package com.shalenmathew.movieflix.core.notifications

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.MIGRATION_3_4
import com.shalenmathew.movieflix.core.utils.MIGRATION_3_5
import com.shalenmathew.movieflix.core.utils.MIGRATION_4_5
import com.shalenmathew.movieflix.core.utils.MIGRATION_5_6
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduledMovieWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val movieId = inputData.getInt(KEY_MOVIE_ID, -1)
            val movieTitle = inputData.getString(KEY_MOVIE_TITLE) ?: "Movie"
            val moviePosterUrl = inputData.getString(KEY_MOVIE_POSTER)
            val movieResultJson = inputData.getString(KEY_MOVIE_RESULT_JSON) ?: ""
            val scheduledDate = inputData.getLong(KEY_SCHEDULED_DATE, 0L)

            if (movieId == -1) {
                return@withContext Result.failure()
            }

            // Show notification
            NotificationHelper.showScheduledMovieNotification(
                context,
                movieId,
                movieTitle,
                moviePosterUrl,
                movieResultJson
            )

            // Wait 10 seconds before removing the schedule
            // This gives user time to see and interact with notification
            kotlinx.coroutines.delay(10000L) // 10 seconds

            // Remove the schedule from database after 10 seconds
            // This allows user to schedule the movie again
            try {
                val database = Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .build()
                
                val scheduledEntity = database.dao.getScheduledMovieById(movieId)
                scheduledEntity?.let {
                    database.dao.deleteScheduledMovie(it)
                }
                
                database.close()
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue even if database cleanup fails
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val KEY_MOVIE_ID = "movie_id"
        const val KEY_MOVIE_TITLE = "movie_title"
        const val KEY_MOVIE_POSTER = "movie_poster"
        const val KEY_MOVIE_RESULT_JSON = "movie_result_json"
        const val KEY_SCHEDULED_DATE = "scheduled_date"
        const val WORK_NAME_PREFIX = "scheduled_movie_"
    }
}
