package com.example.movieflix.domain.usecases

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.entity.IdAndMovieResult
import com.example.movieflix.data.local_storage.entity.WatchListEntity
import com.example.movieflix.domain.repository.WatchListRepository
import javax.inject.Inject

class WatchList @Inject constructor(private val watchListRepository: WatchListRepository) {

    suspend fun insertWatchListData(idAndMovieResult: IdAndMovieResult){
    watchListRepository.insertWatchListData(idAndMovieResult)
}

    suspend fun deleteWatchListData(watchListEntity: WatchListEntity){
        watchListRepository.deleteWatchListData(watchListEntity)
    }

     fun getAllWatchListData():LiveData<List<WatchListEntity>>{
        return  watchListRepository.getAllWatchListData()
    }


}