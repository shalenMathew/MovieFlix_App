package com.example.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local.entity.WatchListEntity

interface WatchListRepository {
  suspend  fun insertWatchListData(watchListEntity: WatchListEntity)

  suspend  fun deleteWatchListData(watchListEntity: WatchListEntity)

   fun getAllWatchListData():LiveData<List<WatchListEntity>>

}