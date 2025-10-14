package com.example.movieflix.data.repository

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.LocalDataSource
import com.example.movieflix.data.local_storage.entity.WatchListEntity
import com.example.movieflix.domain.repository.WatchListRepository

class WatchListRepositoryImpl(private val localDataSource: LocalDataSource):WatchListRepository {
    override suspend fun insertWatchListData(watchListEntity: WatchListEntity) {
        localDataSource.insertWatchListData(watchListEntity)
    }

    override suspend fun deleteWatchListData(watchListEntity: WatchListEntity) {
        localDataSource.deleteWatchListData(watchListEntity)
    }

    override  fun getAllWatchListData(): LiveData<List<WatchListEntity>> {
       return localDataSource.getAllWatchListData()
    }

}