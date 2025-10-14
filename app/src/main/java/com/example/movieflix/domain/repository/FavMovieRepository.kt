package com.example.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.entity.FavouritesEntity

interface FavMovieRepository {
    suspend fun insertFavMovie(favouritesEntity: FavouritesEntity)

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)

    fun getAllFavMovie(): LiveData<List<FavouritesEntity>>
}