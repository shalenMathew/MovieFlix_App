package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieflix.data.local.entity.WatchListEntity
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.usecases.WatchList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WatchListViewModel @Inject constructor(private  val watchList: WatchList):ViewModel(){

//    private var _allWatchListData = MutableLiveData<List<WatchListEntity>>()
//    val allWatchListData:LiveData<List<WatchListEntity>> = _allWatchListData

    fun insertWatchListData(movieResult: MovieResult){

        val watchListEntity = WatchListEntity(movieResult.id!!,movieResult)

        viewModelScope.launch {
            watchList.insertWatchListData(watchListEntity)
        }
    }

    fun getAllWatchListData(): LiveData<List<WatchListEntity>> {
        return watchList.getAllWatchListData()
    }

    fun deleteWatchListData(movieResult: MovieResult){
        val watchListEntity = WatchListEntity(movieResult.id!!,movieResult)
        viewModelScope.launch {
            watchList.deleteWatchListData(watchListEntity)
        }
    }

}