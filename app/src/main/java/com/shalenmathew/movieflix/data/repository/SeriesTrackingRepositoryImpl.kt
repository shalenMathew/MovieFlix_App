package com.shalenmathew.movieflix.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.background.SeriesTrackingWorker
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.data.local_storage.SeriesTrackingDao
import com.shalenmathew.movieflix.data.local_storage.TrackedSeriesWithProgress
import com.shalenmathew.movieflix.data.local_storage.entity.EpisodeTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeasonTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeriesTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeriesProgressEntity
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

                val data = Data.Builder()
                    .putInt(SeriesTrackingWorker.KEY_SERIES_ID, seriesId)
                    .build()
                
                val request = OneTimeWorkRequestBuilder<SeriesTrackingWorker>()
                    .setInputData(data)
                    .build()
                
                workManager.enqueue(request)
                
                emit(NetworkResults.Success(Unit))
            } else {
                emit(NetworkResults.Error(context.getString(R.string.msg_failed_fetch_series)))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: context.getString(R.string.msg_something_went_wrong)))
        }
    }

    override suspend fun untrackSeries(seriesId: Int) {
        // Just delete from tracking table - seasons and episodes will cascade delete.
        // Progress table is NOT touched here.
        val series = seriesTrackingDao.getSeriesById(seriesId)
        series?.let { seriesTrackingDao.deleteSeries(it) }
        seriesTrackingDao.deleteSeasonsBySeriesId(seriesId)
    }

    override suspend fun getSeriesById(seriesId: Int): TrackedSeries? {
        val series = seriesTrackingDao.getSeriesById(seriesId)
        val progress = seriesTrackingDao.getSeriesProgressById(seriesId)
        
        // Return data if either tracking info OR progress info exists
        if (series == null && progress == null) return null

        return TrackedSeries(
            id = seriesId,
            name = series?.name ?: "", // Name might be empty if show is untracked
            posterPath = series?.posterPath,
            backdropPath = series?.backdropPath,
            overview = series?.overview,
            lastWatchedEpisodeId = progress?.lastWatchedEpisodeId,
            lastWatchedSeasonNumber = progress?.lastWatchedSeasonNumber,
            lastWatchedEpisodeNumber = progress?.lastWatchedEpisodeNumber,
            lastUpdated = series?.lastUpdated ?: progress?.lastUpdated ?: System.currentTimeMillis(),
            syncStatus = series?.syncStatus ?: "NONE"
        )
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
        seriesTrackingDao.insertProgress(
            SeriesProgressEntity(
                seriesId = seriesId,
                lastWatchedEpisodeId = episodeId,
                lastWatchedSeasonNumber = seasonNumber,
                lastWatchedEpisodeNumber = episodeNumber
            )
        )
    }

    override suspend fun deleteSeriesProgress(seriesId: Int) {
        seriesTrackingDao.deleteSeriesProgress(seriesId)
    }

    override suspend fun markPreviousEpisodesAsWatched(seriesId: Int, seasonNumber: Int, episodeNumber: Int) {
        seriesTrackingDao.markPreviousEpisodesAsWatched(seriesId, seasonNumber, episodeNumber)
    }

    override suspend fun isSeriesTracked(seriesId: Int): Boolean {
        return seriesTrackingDao.getSeriesById(seriesId) != null
    }

    override suspend fun getEpisodeWatchedStatus(episodeId: Int): Boolean {
        return false // Placeholder
    }

    override suspend fun getTVImages(seriesId: Int): NetworkResults<List<String>> {
        return try {
            val response = remoteDataSource.getTVImages(seriesId)
            if (response.isSuccessful && response.body() != null) {
                val paths = response.body()!!.backdrops?.mapNotNull { it.filePath } ?: emptyList()
                NetworkResults.Success(paths)
            } else {
                NetworkResults.Error(context.getString(R.string.msg_failed_fetch_images))
            }
        } catch (e: Exception) {
            NetworkResults.Error(e.message ?: context.getString(R.string.msg_something_went_wrong))
        }
    }

    override suspend fun updateSeriesBanner(seriesId: Int, bannerPath: String) {
        seriesTrackingDao.updateSeriesBanner(seriesId, bannerPath)
    }

    private fun TrackedSeriesWithProgress.toDomain() = TrackedSeries(
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
        airDate = airDate,
        voteAverage = voteAverage,
        isWatched = isWatched
    )
}
