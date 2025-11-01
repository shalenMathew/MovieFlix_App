package com.shalenmathew.movieflix.data.repository

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.domain.repository.FavMovieRepository

class FavMovieRepositoryImpl(private val localDataSource: LocalDataSource):FavMovieRepository {
    override suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult) {
        localDataSource.insertFavMovie(idAndMovieResult)
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