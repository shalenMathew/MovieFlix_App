package com.shalenmathew.movieflix.data.repository

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
    private val remoteDataSource: RemoteDataSource
) : SeriesTrackingRepository {

    override fun getAllTrackedSeries(): Flow<List<TrackedSeries>> {
        return seriesTrackingDao.getAllTrackedSeries().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun trackSeries(seriesId: Int): Flow<NetworkResults<Unit>> = flow {
        emit(NetworkResults.Loading())
        try {
            val tvDetailResponse = remoteDataSource.getTVDetail(seriesId)
            if (tvDetailResponse.isSuccessful) {
                val tvDetail = tvDetailResponse.body()
                if (tvDetail != null) {
                    val seriesEntity = SeriesTrackingEntity(
                        id = tvDetail.id!!,
                        name = tvDetail.name ?: "",
                        posterPath = tvDetail.poster_path,
                        backdropPath = tvDetail.backdrop_path,
                        overview = tvDetail.overview,
                        lastUpdated = System.currentTimeMillis()
                    )
                    seriesTrackingDao.insertSeries(seriesEntity)

                    val seasons = tvDetail.seasons ?: emptyList()
                    val seasonEntities = mutableListOf<SeasonTrackingEntity>()
                    val episodeEntities = mutableListOf<EpisodeTrackingEntity>()

                    for (seasonBasic in seasons) {
                        if (seasonBasic.season_number != null && seasonBasic.season_number > 0) {
                            val seasonResponse = remoteDataSource.getTVSeason(seriesId, seasonBasic.season_number)
                            if (seasonResponse.isSuccessful) {
                                val seasonDetail = seasonResponse.body()
                                if (seasonDetail != null) {
                                    val seasonId = seasonDetail.id ?: (seriesId * 1000 + seasonBasic.season_number)
                                    seasonEntities.add(
                                        SeasonTrackingEntity(
                                            id = seasonId,
                                            seriesId = seriesId,
                                            seasonNumber = seasonBasic.season_number,
                                            name = seasonDetail.name,
                                            episodeCount = seasonBasic.episode_count,
                                            posterPath = seasonDetail.poster_path
                                        )
                                    )

                                    seasonDetail.episodes?.forEach { episode ->
                                        episodeEntities.add(
                                            EpisodeTrackingEntity(
                                                id = episode.id!!,
                                                seriesId = seriesId,
                                                seasonId = seasonId,
                                                episodeNumber = episode.episode_number ?: 0,
                                                seasonNumber = seasonBasic.season_number,
                                                name = episode.name,
                                                overview = episode.overview,
                                                stillPath = episode.still_path,
                                                runtime = episode.runtime
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    seriesTrackingDao.insertSeasons(seasonEntities)
                    seriesTrackingDao.insertEpisodes(episodeEntities)
                    emit(NetworkResults.Success(Unit))
                } else {
                    emit(NetworkResults.Error("Empty response body"))
                }
            } else {
                emit(NetworkResults.Error("Failed to fetch TV details: ${tvDetailResponse.message()}"))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override suspend fun untrackSeries(seriesId: Int) {
        seriesTrackingDao.deleteTrackedSeriesData(seriesId)
    }

    override fun getSeasonsForSeries(seriesId: Int): Flow<List<TrackedSeason>> {
        return seriesTrackingDao.getSeasonsForSeries(seriesId).map { list ->
            list.map { it.toDomain() }
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

    override suspend fun isSeriesTracked(seriesId: Int): Boolean {
        return seriesTrackingDao.getSeriesById(seriesId) != null
    }

    override suspend fun getEpisodeWatchedStatus(episodeId: Int): Boolean {
        // This might need a new DAO method if not directly available. 
        // For now, let's assume we can check if series is tracked first.
        return false // Placeholder
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
        lastUpdated = lastUpdated
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
