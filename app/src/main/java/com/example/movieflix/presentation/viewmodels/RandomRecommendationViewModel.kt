package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.usecases.RandomRecommendation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RandomRecommendationViewModel @Inject constructor(private val randomRecommendation: RandomRecommendation):ViewModel() {

    private var _searchMovieLiveData=MutableLiveData<NetworkResults<MovieList>>()
    val searchMovieLiveData:LiveData<NetworkResults<MovieList>> = _searchMovieLiveData

    fun fetchSearchMovie(query:String){
        viewModelScope.launch {
            randomRecommendation.searchMovie(query).onEach {
                _searchMovieLiveData.value=it
            }.launchIn(this)
        }
    }


}