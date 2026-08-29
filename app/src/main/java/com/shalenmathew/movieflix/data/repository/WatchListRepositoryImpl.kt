package com.shalenmathew.movieflix.data.repository

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.domain.repository.WatchListRepository

class WatchListRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val seriesTrackingRepository: com.shalenmathew.movieflix.domain.repository.SeriesTrackingRepository
) : WatchListRepository {
    override suspend fun insertWatchListData(idAndMovieResult: IdAndMovieResult) {
        localDataSource.insertWatchListData(idAndMovieResult)
    }

    override suspend fun deleteWatchListData(watchListEntity: WatchListEntity) {
        localDataSource.deleteWatchListData(watchListEntity)
        seriesTrackingRepository.cleanupOrphanedProgress(watchListEntity.id)
    }

    override suspend fun updateWatchListPoster(id: Int, posterPath: String) {
        val watchItem = localDataSource.getWatchListMovieById(id)
        watchItem?.let {
            val updatedMovieResult = it.movieResult.copy(posterPath = posterPath)
            localDataSource.updateWatchListMovieResult(id, updatedMovieResult)
        }
    }

    override suspend fun updateWatchListBanner(id: Int, bannerPath: String) {
        val watchItem = localDataSource.getWatchListMovieById(id)
        watchItem?.let {
            val updatedMovieResult = it.movieResult.copy(backdropPath = bannerPath)
            localDataSource.updateWatchListMovieResult(id, updatedMovieResult)
        }
    }

    override  fun getAllWatchListData(): LiveData<List<WatchListEntity>> {
       return localDataSource.getAllWatchListData()
    }

}