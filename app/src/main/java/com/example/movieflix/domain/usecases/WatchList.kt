package com.example.movieflix.domain.usecases

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local.entity.WatchListEntity
import com.example.movieflix.domain.repository.WatchListRepository
import javax.inject.Inject

class WatchList @Inject constructor(private val watchListRepository: WatchListRepository) {

    suspend fun insertWatchListData(watchListEntity: WatchListEntity){
    watchListRepository.insertWatchListData(watchListEntity)
}

    suspend fun deleteWatchListData(watchListEntity: WatchListEntity){
        watchListRepository.deleteWatchListData(watchListEntity)
    }

     fun getAllWatchListData():LiveData<List<WatchListEntity>>{
        return  watchListRepository.getAllWatchListData()
    }


}