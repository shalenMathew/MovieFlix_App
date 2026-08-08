package com.shalenmathew.movieflix.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.shalenmathew.movieflix.core.background.SeriesTrackingWorker
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.data.local_storage.SeriesTrackingDao
import com.shalenmathew.movieflix.data.local_storage.entity.EpisodeTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeasonTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeriesTrackingEntity
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.domain.model.TrackedEpisode
import com.shalenmathew.movieflix.domain.model.TrackedSeason
import com.shalenmathew.movieflix.domain.model.TrackedSeries
import com.shalenmathew.movieflix.domain.repository.SeriesTrackingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SeriesTrackingRepositoryImpl @Inject constructor(
    private val seriesTrackingDao: SeriesTrackingDao,
    private val remoteDataSource: RemoteDataSource,
    private val context: Context
) : SeriesTrackingRepository {

    private val workManager = WorkManager.getInstance(context)

    override fun getAllTrackedSeries(): Flow<List<TrackedSeries>> {
        return seriesTrackingDao.getAllTrackedSeries().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun trackSeries(seriesId: Int): Flow<NetworkResults<Unit>> = flow {
        emit(NetworkResults.Loading())
        try {
            // Immediately insert basic series info with PENDING status
            val tvDetailResponse = remoteDataSource.getTVDetail(seriesId)
            if (tvDetailResponse.isSuccessful && tvDetailResponse.body() != null) {
                val tvDetail = tvDetailResponse.body()!!
                val seriesEntity = SeriesTrackingEntity(
                    id = tvDetail.id!!,
                    name = tvDetail.name ?: "",
                    posterPath = tvDetail.poster_path,
                    backdropPath = tvDetail.backdrop_path,
                    overview = tvDetail.overview,
                    syncStatus = "PENDING"
                )
                seriesTrackingDao.insertSeries(seriesEntity)

                // Kick off background worker for seasons and episodes
                val data = Data.Builder()
                    .putInt(SeriesTrackingWorker.KEY_SERIES_ID, seriesId)
                    .build()
                
                val request = OneTimeWorkRequestBuilder<SeriesTrackingWorker>()
                    .setInputData(data)
                    .build()
                
                workManager.enqueue(request)
                
                emit(NetworkResults.Success(Unit))
            } else {
                emit(NetworkResults.Error("Failed to fetch series info"))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: "Unknown error"))
        }
    }

    override suspend fun untrackSeries(seriesId: Int) {
        seriesTrackingDao.deleteTrackedSeriesData(seriesId)
    }

    override fun getSeasonsForSeries(seriesId: Int): Flow<List<TrackedSeason>> {
        return seriesTrackingDao.getSeasonsWithProgress(seriesId).map { list ->
            list.map { 
                TrackedSeason(
                    id = it.id,
                    seriesId = it.seriesId,
                    seasonNumber = it.seasonNumber,
                    name = it.name,
                    episodeCount = it.episodeCount,
                    posterPath = it.posterPath,
                    watchedCount = it.watchedCount
                )
            }
        }
    }

    override fun getEpisodesForSeason(seasonId: Int): Flow<List<TrackedEpisode>> {
        return seriesTrackingDao.getEpisodesForSeason(seasonId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun updateEpisodeWatchedStatus(episodeId: Int, isWatched: Boolean) {
        seriesTrackingDao.updateEpisodeWatchedStatus(episodeId, isWatched)
    }

    override suspend fun updateLastWatchedEpisode(seriesId: Int, episodeId: Int, seasonNumber: Int, episodeNumber: Int) {
        seriesTrackingDao.updateLastWatchedEpisode(seriesId, episodeId, seasonNumber, episodeNumber)
    }

    override suspend fun markPreviousEpisodesAsWatched(seriesId: Int, seasonNumber: Int, episodeNumber: Int) {
        seriesTrackingDao.markPreviousEpisodesAsWatched(seriesId, seasonNumber, episodeNumber)
    }

    override suspend fun isSeriesTracked(seriesId: Int): Boolean {
        return seriesTrackingDao.getSeriesById(seriesId) != null
    }

    override suspend fun getEpisodeWatchedStatus(episodeId: Int): Boolean {
        // This might need a new DAO method if not directly available. 
        // For now, let's assume we can check if series is tracked first.
        return false // Placeholder
    }

    override suspend fun getTVImages(seriesId: Int): NetworkResults<List<String>> {
        return try {
            val response = remoteDataSource.getTVImages(seriesId)
            if (response.isSuccessful && response.body() != null) {
                val paths = response.body()!!.backdrops?.mapNotNull { it.filePath } ?: emptyList()
                NetworkResults.Success(paths)
            } else {
                NetworkResults.Error("Failed to fetch images")
            }
        } catch (e: Exception) {
            NetworkResults.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun updateSeriesBanner(seriesId: Int, bannerPath: String) {
        val series = seriesTrackingDao.getSeriesById(seriesId)
        series?.let {
            seriesTrackingDao.insertSeries(it.copy(backdropPath = bannerPath))
        }
    }

    private fun SeriesTrackingEntity.toDomain() = TrackedSeries(
        id = id,
        name = name,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        lastWatchedEpisodeId = lastWatchedEpisodeId,
        lastWatchedSeasonNumber = lastWatchedSeasonNumber,
        lastWatchedEpisodeNumber = lastWatchedEpisodeNumber,
        lastUpdated = lastUpdated,
        syncStatus = syncStatus
    )

    private fun SeasonTrackingEntity.toDomain() = TrackedSeason(
        id = id,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        name = name,
        episodeCount = episodeCount,
        posterPath = posterPath
    )

    private fun EpisodeTrackingEntity.toDomain() = TrackedEpisode(
        id = id,
        seriesId = seriesId,
        seasonId = seasonId,
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        name = name,
        overview = overview,
        stillPath = stillPath,
        runtime = runtime,
        isWatched = isWatched
    )
}
