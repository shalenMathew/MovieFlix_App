package com.example.movieflix.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.movieflix.data.local_storage.entity.IdAndMovieResult
import com.example.movieflix.data.local_storage.entity.WatchListEntity
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.usecases.WatchList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WatchListViewModel @Inject constructor(private val watchList: WatchList) : ViewModel() {

//    private var _allWatchListData = MutableLiveData<List<WatchListEntity>>()
//    val allWatchListData:LiveData<List<WatchListEntity>> = _allWatchListData

    fun insertWatchListData(movieResult: MovieResult) {

        val watchListData = IdAndMovieResult(movieResult.id!!, movieResult)

        viewModelScope.launch {
            watchList.insertWatchListData(watchListData)
        }
    }

    fun getAllWatchListData(): LiveData<List<WatchListEntity>> {
        return watchList.getAllWatchListData().map { list ->
            list.sortedByDescending { item -> item.insertedAt }
        }
    }

    fun deleteWatchListData(movieResult: MovieResult) {
        val watchListEntity = WatchListEntity(movieResult.id!!, movieResult, null)
        viewModelScope.launch {
            watchList.deleteWatchListData(watchListEntity)
        }
    }

}