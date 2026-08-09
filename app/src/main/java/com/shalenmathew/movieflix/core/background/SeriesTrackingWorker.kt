package com.shalenmathew.movieflix.core.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shalenmathew.movieflix.data.local_storage.SeriesTrackingDao
import com.shalenmathew.movieflix.data.local_storage.entity.EpisodeTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeasonTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeriesTrackingEntity
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeriesTrackingWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SeriesTrackingWorkerEntryPoint {
        fun seriesTrackingDao(): SeriesTrackingDao
        fun remoteDataSource(): RemoteDataSource
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val seriesId = inputData.getInt(KEY_SERIES_ID, -1)
        if (seriesId == -1) return@withContext Result.failure()

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SeriesTrackingWorkerEntryPoint::class.java
        )
        val dao = entryPoint.seriesTrackingDao()
        val remote = entryPoint.remoteDataSource()

        try {
            val response = remote.getTVDetail(seriesId)
            if (!response.isSuccessful || response.body() == null) {
                dao.updateSyncStatus(seriesId, "FAILED")
                return@withContext Result.retry()
            }

            val tvDetail = response.body()!!
            val seriesEntity = SeriesTrackingEntity(
                id = tvDetail.id!!,
                name = tvDetail.name ?: "",
                posterPath = tvDetail.poster_path,
                backdropPath = tvDetail.backdrop_path,
                overview = tvDetail.overview,
                syncStatus = "PENDING"
            )
            dao.insertSeries(seriesEntity)

            val seasons = tvDetail.seasons ?: emptyList()
            val seasonEntities = mutableListOf<SeasonTrackingEntity>()
            val episodeEntities = mutableListOf<EpisodeTrackingEntity>()

            for (seasonBasic in seasons) {
                val sNum = seasonBasic.season_number
                if (sNum != null && sNum > 0) {
                    val sResponse = remote.getTVSeason(seriesId, sNum)
                    if (sResponse.isSuccessful && sResponse.body() != null) {
                        val seasonDetail = sResponse.body()!!
                        val seasonId = seasonDetail.id ?: (seriesId * 1000 + sNum)
                        
                        seasonEntities.add(
                            SeasonTrackingEntity(
                                id = seasonId,
                                seriesId = seriesId,
                                seasonNumber = sNum,
                                name = seasonDetail.name,
                                episodeCount = seasonBasic.episode_count,
                                posterPath = seasonDetail.poster_path
                            )
                        )

                        seasonDetail.episodes?.forEach { ep ->
                            episodeEntities.add(
                                EpisodeTrackingEntity(
                                    id = ep.id!!,
                                    seriesId = seriesId,
                                    seasonId = seasonId,
                                    episodeNumber = ep.episode_number ?: 0,
                                    seasonNumber = sNum,
                                    name = ep.name,
                                    overview = ep.overview,
                                    stillPath = ep.still_path,
                                    runtime = ep.runtime,
                                    airDate = ep.air_date,
                                    voteAverage = ep.vote_average
                                )
                            )
                        }
                    }
                }
            }

            // Transactional-like behavior: insert everything and then mark as COMPLETED
            dao.insertSeasons(seasonEntities)
            dao.insertEpisodes(episodeEntities)
            dao.updateSyncStatus(seriesId, "COMPLETED")

            Result.success()
        } catch (e: Exception) {
            dao.updateSyncStatus(seriesId, "FAILED")
            Result.retry()
        }
    }

    companion object {
        const val KEY_SERIES_ID = "series_id"
    }
}
