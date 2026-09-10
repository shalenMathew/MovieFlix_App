package com.shalenmathew.movieflix.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shalenmathew.movieflix.data.local_storage.MovieDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@HiltWorker
class ScheduledMovieWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val movieDao: MovieDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val movieId = inputData.getInt(KEY_MOVIE_ID, -1)
            val movieTitle = inputData.getString(KEY_MOVIE_TITLE) ?: "Movie"
            val moviePosterUrl = inputData.getString(KEY_MOVIE_POSTER)
            val customMessage = inputData.getString(KEY_CUSTOM_MESSAGE)

            if (movieId == -1) {
                return@withContext Result.failure()
            }

            // Show notification
            NotificationHelper.showScheduledMovieNotification(
                context,
                movieId,
                movieTitle,
                moviePosterUrl,
                customMessage
            )

            // Wait 10 seconds before removing the schedule
            // This gives user time to see and interact with notification
            delay(10000L) // 10 seconds

            // Remove the schedule from database after 10 seconds
            // This allows user to schedule the movie again
            try {
                val scheduledEntity = movieDao.getScheduledMovieById(movieId)
                scheduledEntity?.let {
                    movieDao.deleteScheduledMovie(it)
                }
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
        const val KEY_CUSTOM_MESSAGE = "custom_message"
        const val WORK_NAME_PREFIX = "scheduled_movie_"
    }
}
