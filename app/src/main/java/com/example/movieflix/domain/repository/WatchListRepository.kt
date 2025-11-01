package com.example.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.entity.IdAndMovieResult
import com.example.movieflix.data.local_storage.entity.WatchListEntity

interface WatchListRepository {

  suspend  fun insertWatchListData(idAndMovieResult: IdAndMovieResult)

  suspend  fun deleteWatchListData(watchListEntity: WatchListEntity)

   fun getAllWatchListData():LiveData<List<WatchListEntity>>

}