package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.usecases.SearchMovie
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchMovieViewModel @Inject constructor (private val searchMovie: SearchMovie):ViewModel() {

    private var _searchMovieLiveData=MutableLiveData<NetworkResults<MovieList>>()
     val searchMovieLiveData:LiveData<NetworkResults<MovieList>> = _searchMovieLiveData

    private var _trendingMovies=MutableLiveData<NetworkResults<MovieList>>()
    val trendingMovies:LiveData<NetworkResults<MovieList>> = _trendingMovies

    fun fetchSearchMovie(query:String){
        viewModelScope.launch {
            searchMovie.searchMovie(query).onEach {
              _searchMovieLiveData.value=it
            }.launchIn(this)
        }
    }

    fun fetchTrendingMovies(){
        viewModelScope.launch {
            searchMovie.trendingMovies().onEach {
               _trendingMovies.value=it
            }.launchIn(this)
        }
    }

}