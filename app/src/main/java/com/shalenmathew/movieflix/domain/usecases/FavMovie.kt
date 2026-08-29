package com.shalenmathew.movieflix.domain.usecases

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.domain.repository.FavMovieRepository
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

    suspend fun updateFavMoviePoster(id: Int, posterPath: String) {
        favMovieRepository.updateFavMoviePoster(id, posterPath)
    }

    suspend fun updateFavMovieBanner(id: Int, bannerPath: String) {
        favMovieRepository.updateFavMovieBanner(id, bannerPath)
    }

    suspend fun addPersonalNote(id: Int, personalNote: String?) {
        return favMovieRepository.addPersonalNote(id, personalNote)
    }

}