package com.example.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.entity.ScheduledEntity

interface ScheduledRepository {

    suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity)

    suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity)

    fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>>

    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity?

}
