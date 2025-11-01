package com.example.movieflix.domain.usecases

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.entity.FavouritesEntity
import com.example.movieflix.data.local_storage.entity.IdAndMovieResult
import com.example.movieflix.domain.repository.FavMovieRepository
import javax.inject.Inject

class FavMovie @Inject constructor(private val favMovieRepository: FavMovieRepository) {

    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult){
        favMovieRepository.insertFavMovie(idAndMovieResult)
    }

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity){
        favMovieRepository.deleteFavMovie(favouritesEntity)
    }

    fun getAllFavMovie(): LiveData<List<FavouritesEntity>>{
        return favMovieRepository.getAllFavMovie()
    }

    suspend fun addPersonalNote(id: Int, personalNote: String?) {
        return favMovieRepository.addPersonalNote(id, personalNote)
    }

}