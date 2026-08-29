package com.shalenmathew.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity

interface WatchListRepository {

  suspend  fun insertWatchListData(idAndMovieResult: IdAndMovieResult)

  suspend  fun deleteWatchListData(watchListEntity: WatchListEntity)

  suspend fun updateWatchListPoster(id: Int, posterPath: String)

  suspend fun updateWatchListBanner(id: Int, bannerPath: String)

   fun getAllWatchListData():LiveData<List<WatchListEntity>>

}