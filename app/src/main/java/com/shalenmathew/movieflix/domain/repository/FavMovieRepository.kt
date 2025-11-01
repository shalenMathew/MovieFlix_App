package com.shalenmathew.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult

interface FavMovieRepository {
    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult)

    suspend fun addPersonalNote(id: Int, personalNote: String?)

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)

    fun getAllFavMovie(): LiveData<List<FavouritesEntity>>
}