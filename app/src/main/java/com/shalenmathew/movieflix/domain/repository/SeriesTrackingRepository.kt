package com.shalenmathew.movieflix.domain.repository

import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.TrackedEpisode
import com.shalenmathew.movieflix.domain.model.TrackedSeason
import com.shalenmathew.movieflix.domain.model.TrackedSeries
import kotlinx.coroutines.flow.Flow

interface SeriesTrackingRepository {

    fun getAllTrackedSeries(): Flow<List<TrackedSeries>>

    suspend fun trackSeries(seriesId: Int): Flow<NetworkResults<Unit>>

    suspend fun untrackSeries(seriesId: Int)

    suspend fun getSeriesById(seriesId: Int): TrackedSeries?

    fun getSeasonsForSeries(seriesId: Int): Flow<List<TrackedSeason>>

    fun getEpisodesForSeason(seasonId: Int): Flow<List<TrackedEpisode>>

    suspend fun updateEpisodeWatchedStatus(episodeId: Int, isWatched: Boolean)

    suspend fun updateLastWatchedEpisode(seriesId: Int, episodeId: Int, seasonNumber: Int, episodeNumber: Int)

    suspend fun deleteSeriesProgress(seriesId: Int)

    suspend fun markPreviousEpisodesAsWatched(seriesId: Int, seasonNumber: Int, episodeNumber: Int)

    suspend fun isSeriesTracked(seriesId: Int): Boolean

    suspend fun getEpisodeWatchedStatus(episodeId: Int): Boolean

    suspend fun getTVImages(seriesId: Int): NetworkResults<List<String>>

    suspend fun updateSeriesBanner(seriesId: Int, bannerPath: String)
}
