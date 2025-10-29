package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.movieflix.data.local_storage.entity.FavouritesEntity
import com.example.movieflix.data.local_storage.entity.IdAndMovieResult
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.usecases.FavMovie
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

}