package com.shalenmathew.movieflix.data.repository

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.domain.repository.ScheduledRepository

class ScheduledRepositoryImpl(private val localDataSource: LocalDataSource) : ScheduledRepository {
    override suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity) {
        localDataSource.insertScheduledMovie(scheduledEntity)
    }

    override suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity) {
        localDataSource.deleteScheduledMovie(scheduledEntity)
    }

    override fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return localDataSource.getAllScheduledMovies()
    }

    override suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return localDataSource.getScheduledMovieById(movieId)
    }
}
