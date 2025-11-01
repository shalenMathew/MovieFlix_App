package com.shalenmathew.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenmathew.movieflix.core.notifications.MovieScheduler
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.usecases.ScheduledMovies
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduledViewModel @Inject constructor(
    private val scheduledMovies: ScheduledMovies,
    private val movieScheduler: MovieScheduler
) : ViewModel() {

    fun insertScheduledMovie(movieResult: MovieResult, scheduledDate: Long) {
        val scheduledEntity = ScheduledEntity(movieResult.id!!, movieResult, scheduledDate)
        viewModelScope.launch {
            scheduledMovies.insertScheduledMovie(scheduledEntity)
            // Schedule the notification
            movieScheduler.scheduleMovieNotification(movieResult, scheduledDate)
        }
    }

    fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return scheduledMovies.getAllScheduledMovies()
    }

    fun deleteScheduledMovie(movieResult: MovieResult, scheduledDate: Long) {
        val scheduledEntity = ScheduledEntity(movieResult.id!!, movieResult, scheduledDate)
        viewModelScope.launch {
            scheduledMovies.deleteScheduledMovie(scheduledEntity)
            // Cancel the scheduled notification
            movieResult.id?.let { movieScheduler.cancelMovieNotification(it) }
        }
    }

    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return scheduledMovies.getScheduledMovieById(movieId)
    }

}
