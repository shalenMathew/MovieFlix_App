package com.shalenmathew.movieflix.domain.usecases

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.domain.repository.ScheduledRepository
import javax.inject.Inject

class ScheduledMovies @Inject constructor(private val scheduledRepository: ScheduledRepository) {

    suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity) {
        scheduledRepository.insertScheduledMovie(scheduledEntity)
    }

    suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity) {
        scheduledRepository.deleteScheduledMovie(scheduledEntity)
    }

    fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return scheduledRepository.getAllScheduledMovies()
    }

    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return scheduledRepository.getScheduledMovieById(movieId)
    }

}
