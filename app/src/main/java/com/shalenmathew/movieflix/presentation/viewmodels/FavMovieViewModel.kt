package com.shalenmathew.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.usecases.FavMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavMovieViewModel @Inject constructor(private val favMovie: FavMovie) : ViewModel() {

    fun insertFavMovieData(movieResult: MovieResult) {
        val favMovieData = IdAndMovieResult(movieResult.id!!, movieResult)
        viewModelScope.launch {
            favMovie.insertFavMovie(favMovieData)
        }
    }

    fun getAllMovieData(): LiveData<List<FavouritesEntity>> {
        return favMovie.getAllFavMovie().map { list ->
            list.sortedByDescending { item -> item.insertedAt }
        }
    }

    fun deleteWatchListData(movieResult: MovieResult) {
        val favMovieEntity = FavouritesEntity(movieResult.id!!, movieResult, insertedAt = null)
        viewModelScope.launch {
            favMovie.deleteFavMovie(favMovieEntity)
        }
    }

    fun addPersonalNote(id: Int, personalNote: String?) {
        viewModelScope.launch {
            favMovie.addPersonalNote(id, personalNote)
        }
    }

    fun updateFavPoster(id: Int, posterPath: String) {
        viewModelScope.launch {
            favMovie.updateFavMoviePoster(id, posterPath)
        }
    }

    fun updateFavBanner(id: Int, bannerPath: String) {
        viewModelScope.launch {
            favMovie.updateFavMovieBanner(id, bannerPath)
        }
    }

    fun insertGalleryImage(id: Int, imagePath: String) {
        viewModelScope.launch {
            favMovie.insertGalleryImage(com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity(mediaId = id, imagePath = imagePath))
        }
    }

    fun deleteGalleryImage(image: com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity) {
        viewModelScope.launch {
            favMovie.deleteGalleryImage(image)
        }
    }

    fun getGalleryImages(mediaId: Int): LiveData<List<com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity>> {
        return favMovie.getGalleryImagesForMedia(mediaId)
    }

}