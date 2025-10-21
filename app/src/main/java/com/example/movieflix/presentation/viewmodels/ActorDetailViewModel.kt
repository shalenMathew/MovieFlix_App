package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.ActorDetail
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.usecases.GetActorInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActorDetailViewModel @Inject constructor(
    private val getActorInfo: GetActorInfo
) : ViewModel() {

    private val _actorDetail = MutableLiveData<NetworkResults<ActorDetail>>()
    val actorDetail: LiveData<NetworkResults<ActorDetail>> = _actorDetail

    private val _actorMoviesAndShows = MutableLiveData<NetworkResults<List<MovieResult>>>()
    val actorMoviesAndShows: LiveData<NetworkResults<List<MovieResult>>> = _actorMoviesAndShows

    fun loadActorDetail(personId: Int) {
        viewModelScope.launch {
            getActorInfo.getActorDetail(personId).onEach {
                _actorDetail.value = it
            }.launchIn(this)
        }
    }

    fun loadActorMoviesAndShows(personId: Int) {
        viewModelScope.launch {
            getActorInfo.getActorMoviesAndShows(personId).onEach {
                _actorMoviesAndShows.value = it
            }.launchIn(this)
        }
    }
}
