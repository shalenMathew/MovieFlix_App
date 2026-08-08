package com.shalenmathew.movieflix.data.local_storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shalenmathew.movieflix.data.local_storage.entity.EpisodeTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeasonTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeriesTrackingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesTrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesTrackingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(seasons: List<SeasonTrackingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeTrackingEntity>)

    @Query("SELECT * FROM series_tracking_table ORDER BY lastUpdated DESC")
    fun getAllTrackedSeries(): Flow<List<SeriesTrackingEntity>>

    @Query("SELECT * FROM series_tracking_table WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: Int): SeriesTrackingEntity?

    @Query("SELECT * FROM seasons_tracking_table WHERE seriesId = :seriesId ORDER BY seasonNumber ASC")
    fun getSeasonsForSeries(seriesId: Int): Flow<List<SeasonTrackingEntity>>

    @Query("""
        SELECT s.*, (SELECT COUNT(*) FROM episodes_tracking_table e WHERE e.seasonId = s.id AND e.isWatched = 1) as watchedCount 
        FROM seasons_tracking_table s 
        WHERE s.seriesId = :seriesId 
        ORDER BY s.seasonNumber ASC
    """)
    fun getSeasonsWithProgress(seriesId: Int): Flow<List<SeasonWithProgress>>

    @Query("SELECT COUNT(*) FROM episodes_tracking_table WHERE seasonId = :seasonId AND isWatched = 1")
    fun getWatchedEpisodeCountForSeason(seasonId: Int): Flow<Int>

    @Query("SELECT * FROM episodes_tracking_table WHERE seasonId = :seasonId ORDER BY episodeNumber ASC")
    fun getEpisodesForSeason(seasonId: Int): Flow<List<EpisodeTrackingEntity>>

    @Query("SELECT * FROM episodes_tracking_table WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getAllEpisodesForSeries(seriesId: Int): Flow<List<EpisodeTrackingEntity>>

    @Query("UPDATE episodes_tracking_table SET isWatched = :isWatched WHERE id = :episodeId")
    suspend fun updateEpisodeWatchedStatus(episodeId: Int, isWatched: Boolean)

    @Query("UPDATE series_tracking_table SET lastWatchedEpisodeId = :episodeId, lastWatchedSeasonNumber = :seasonNumber, lastWatchedEpisodeNumber = :episodeNumber, lastUpdated = :timestamp WHERE id = :seriesId")
    suspend fun updateLastWatchedEpisode(seriesId: Int, episodeId: Int, seasonNumber: Int, episodeNumber: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE episodes_tracking_table SET isWatched = 1 WHERE seriesId = :seriesId AND (seasonNumber < :seasonNumber OR (seasonNumber = :seasonNumber AND episodeNumber <= :episodeNumber))")
    suspend fun markPreviousEpisodesAsWatched(seriesId: Int, seasonNumber: Int, episodeNumber: Int)

    @Query("UPDATE series_tracking_table SET syncStatus = :status WHERE id = :seriesId")
    suspend fun updateSyncStatus(seriesId: Int, status: String)

    @Delete
    suspend fun deleteSeries(series: SeriesTrackingEntity)

    @Transaction
    suspend fun deleteTrackedSeriesData(seriesId: Int) {
        val series = getSeriesById(seriesId)
        series?.let { deleteSeries(it) }
    }
}

data class SeasonWithProgress(
    val id: Int,
    val seriesId: Int,
    val seasonNumber: Int,
    val name: String?,
    val episodeCount: Int?,
    val posterPath: String?,
    val watchedCount: Int
)
