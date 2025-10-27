package com.example.movieflix.data.repository

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.LocalDataSource
import com.example.movieflix.data.local_storage.entity.FavouritesEntity
import com.example.movieflix.domain.repository.FavMovieRepository

class FavMovieRepositoryImpl(private val localDataSource: LocalDataSource):FavMovieRepository {
    override suspend fun insertFavMovie(favouritesEntity: FavouritesEntity) {
        localDataSource.insertFavMovie(favouritesEntity)
    }

    override suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity) {
      localDataSource.deleteFavMovie(favouritesEntity)
    }

    override fun getAllFavMovie(): LiveData<List<FavouritesEntity>> {
       return localDataSource.getAllFavMovie()
    }

    override suspend fun addPersonalNote(id: Int, personalNote: String?) {
        return localDataSource.addPersonalNote(id, personalNote)
    }
}