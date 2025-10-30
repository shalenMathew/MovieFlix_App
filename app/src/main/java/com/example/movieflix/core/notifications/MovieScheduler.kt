package com.example.movieflix.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.domain.model.MovieResult
import com.google.gson.Gson
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MovieScheduler @Inject constructor(
    private val context: Context
) {

    fun scheduleMovieNotification(movieResult: MovieResult, scheduledTimeMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val delay = scheduledTimeMillis - currentTime

        if (delay <= 0) {
            // Time has already passed, don't schedule
            return
        }

        val movieResultJson = Gson().toJson(movieResult)
        
        // Construct full poster URL for notification image
        val posterUrl = movieResult.posterPath?.let { 
            Constants.TMDB_IMAGE_BASE_URL_W780 + it 
        }
        
        val inputData = Data.Builder()
            .putInt(ScheduledMovieWorker.KEY_MOVIE_ID, movieResult.id ?: 0)
            .putString(ScheduledMovieWorker.KEY_MOVIE_TITLE, movieResult.title ?: movieResult.name ?: "Movie")
            .putString(ScheduledMovieWorker.KEY_MOVIE_POSTER, posterUrl)
            .putString(ScheduledMovieWorker.KEY_MOVIE_RESULT_JSON, movieResultJson)
            .putLong(ScheduledMovieWorker.KEY_SCHEDULED_DATE, scheduledTimeMillis)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ScheduledMovieWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(SCHEDULED_WORK_TAG)
            .build()

        val workName = "${ScheduledMovieWorker.WORK_NAME_PREFIX}${movieResult.id}"

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelMovieNotification(movieId: Int) {
        val workName = "${ScheduledMovieWorker.WORK_NAME_PREFIX}$movieId"
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    companion object {
        private const val SCHEDULED_WORK_TAG = "scheduled_movies"
    }
}
