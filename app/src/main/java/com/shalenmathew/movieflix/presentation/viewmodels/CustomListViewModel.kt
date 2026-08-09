package com.shalenmathew.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.shalenmathew.movieflix.domain.model.CustomListMovie
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.model.UserCustomList
import com.shalenmathew.movieflix.domain.repository.CustomListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomListViewModel @Inject constructor(
    private val repository: CustomListRepository
) : ViewModel() {

    val allLists: LiveData<List<UserCustomList>> = repository.getAllLists().asLiveData()

    fun createList(name: String, description: String?) {
        viewModelScope.launch {
            repository.createList(name, description)
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            repository.deleteList(listId)
        }
    }

    fun addMovieToList(listId: Int, movie: MovieResult) {
        viewModelScope.launch {
            repository.addMovieToList(listId, movie)
        }
    }

    fun removeMovieFromList(listId: Int, mediaId: Int) {
        viewModelScope.launch {
            repository.removeMovieFromList(listId, mediaId)
        }
    }

    fun getMoviesInList(listId: Int): LiveData<List<CustomListMovie>> {
        return repository.getMoviesInList(listId).asLiveData()
    }

    suspend fun isMovieInList(listId: Int, mediaId: Int): Boolean {
        return repository.isMovieInList(listId, mediaId)
    }
}
