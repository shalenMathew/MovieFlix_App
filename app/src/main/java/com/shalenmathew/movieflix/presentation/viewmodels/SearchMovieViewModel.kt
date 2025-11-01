package com.shalenmathew.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.MovieList
import com.shalenmathew.movieflix.domain.usecases.SearchMovie
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


    init {
        fetchTrendingMovies()
    }

    fun fetchSearchMovie(query:String){
        viewModelScope.launch {
            searchMovie.searchMovie(query).onEach {
              _searchMovieLiveData.value=it
            }.launchIn(this)
        }
    }

    fun fetchTrendingMovies(){

        if (_trendingMovies.value is NetworkResults.Loading || _trendingMovies.value is NetworkResults.Success) {
            return
        }
        viewModelScope.launch {
            searchMovie.trendingMovies().onEach {
               _trendingMovies.value=it
            }.launchIn(this)
        }
    }

}