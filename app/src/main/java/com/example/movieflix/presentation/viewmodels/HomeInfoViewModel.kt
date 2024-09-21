package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.model.MovieVideoResultList
import com.example.movieflix.domain.model.WatchProviders
import com.example.movieflix.domain.usecases.GetMovieInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeInfoViewModel @Inject constructor(private val getMovieInfo: GetMovieInfo):ViewModel() {

    private var _homeFeedList:MutableLiveData<NetworkResults<HomeFeedData>> = MutableLiveData()
    var homeFeedList:LiveData<NetworkResults<HomeFeedData>> = _homeFeedList

    private var _movieTrailerList = MutableLiveData<NetworkResults<MovieVideoResultList>>()
    val movieTrailerList:LiveData<NetworkResults<MovieVideoResultList>> = _movieTrailerList

    private var _recommendationList=MutableLiveData<NetworkResults<MovieList>>()
    val recommendationLiveData:LiveData<NetworkResults<MovieList>> = _recommendationList

    private var _whereToWatchProvider=MutableLiveData<NetworkResults<WatchProviders>>()
    val whereToWatchProviders:LiveData<NetworkResults<WatchProviders>> = _whereToWatchProvider

    init {
        getMovieInfoData() // call the function as soon the viewmodel is initialized
    }

 fun getMovieInfoData(){
    viewModelScope.launch {
        getMovieInfo.getMovieInfo().onEach {  // onEach -> each time a data is emitted the below block will run
            _homeFeedList.value=it
        }.launchIn(this) // -> launchIn is used to launch the collection of the Flow inside a specific coroutine scope.
    // Instead of manually calling 'collect', launchIn automatically starts collecting the Flow in the scope you specify.

        // launchIn vs Collect
        // launchIn is a non blocking in nature which mean code below launchIn will still run even when all values are not emitted
        // whereas the code below or next line below .collect() won't run until all emitted values is being /
    }
}

    fun getMovieTrailer(movieId:Int){
        viewModelScope.launch {
            getMovieInfo.getMovieTrailer(movieId).onEach {
                _movieTrailerList.value = it
            }.launchIn(this)
        }
    }

    fun getRecommendation(movieId: Int){
        viewModelScope.launch {
            getMovieInfo.getRecommendation(movieId).onEach {
                _recommendationList.value=it
            }.launchIn(this)
        }
    }

    fun getWhereToWatchProvider(movieId:Int){
        viewModelScope.launch {
            getMovieInfo.getWhereToWatchProviders(movieId).onEach {
                _whereToWatchProvider.value=it
            }.launchIn(this)
        }
    }

}