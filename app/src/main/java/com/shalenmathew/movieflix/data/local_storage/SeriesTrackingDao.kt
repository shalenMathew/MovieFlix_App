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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: com.shalenmathew.movieflix.data.local_storage.entity.SeriesProgressEntity)

    @Query("""
        SELECT s.*, p.lastWatchedEpisodeId, p.lastWatchedSeasonNumber, p.lastWatchedEpisodeNumber 
        FROM series_tracking_table s 
        LEFT JOIN series_progress_table p ON s.id = p.seriesId 
        ORDER BY s.lastUpdated DESC
    """)
    fun getAllTrackedSeries(): Flow<List<TrackedSeriesWithProgress>>

    @Query("SELECT * FROM series_tracking_table WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: Int): SeriesTrackingEntity?

    @Query("SELECT * FROM series_progress_table WHERE seriesId = :seriesId")
    suspend fun getSeriesProgressById(seriesId: Int): com.shalenmathew.movieflix.data.local_storage.entity.SeriesProgressEntity?

    @Query("DELETE FROM series_progress_table WHERE seriesId = :seriesId")
    suspend fun deleteSeriesProgress(seriesId: Int)

    @Query("SELECT * FROM seasons_tracking_table WHERE seriesId = :seriesId ORDER BY seasonNumber ASC")
    fun getSeasonsForSeries(seriesId: Int): Flow<List<SeasonTrackingEntity>>

    @Query("DELETE FROM seasons_tracking_table WHERE seriesId = :seriesId")
    suspend fun deleteSeasonsBySeriesId(seriesId: Int)

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

    @Query("UPDATE episodes_tracking_table SET isWatched = 1 WHERE seriesId = :seriesId AND (seasonNumber < :seasonNumber OR (seasonNumber = :seasonNumber AND episodeNumber <= :episodeNumber))")
    suspend fun markPreviousEpisodesAsWatched(seriesId: Int, seasonNumber: Int, episodeNumber: Int)

    @Query("UPDATE series_tracking_table SET syncStatus = :status WHERE id = :seriesId")
    suspend fun updateSyncStatus(seriesId: Int, status: String)

    @Query("UPDATE series_tracking_table SET backdropPath = :bannerPath WHERE id = :seriesId")
    suspend fun updateSeriesBanner(seriesId: Int, bannerPath: String)

    @Delete
    suspend fun deleteSeries(series: SeriesTrackingEntity)

    @Transaction
    suspend fun deleteTrackedSeriesData(seriesId: Int) {
        val series = getSeriesById(seriesId)
        series?.let { 
            deleteSeries(it) 
            deleteSeasonsBySeriesId(seriesId)
        }
    }

    @Query("SELECT * FROM series_tracking_table")
    suspend fun getAllSeriesSync(): List<SeriesTrackingEntity>

    @Query("SELECT * FROM seasons_tracking_table")
    suspend fun getAllSeasonsSync(): List<SeasonTrackingEntity>

    @Query("SELECT * FROM episodes_tracking_table")
    suspend fun getAllEpisodesSync(): List<EpisodeTrackingEntity>

    @Query("SELECT * FROM series_progress_table")
    suspend fun getAllProgressSync(): List<com.shalenmathew.movieflix.data.local_storage.entity.SeriesProgressEntity>

    @Query("DELETE FROM series_tracking_table")
    suspend fun deleteAllSeries()

    @Query("DELETE FROM seasons_tracking_table")
    suspend fun deleteAllSeasons()

    @Query("DELETE FROM episodes_tracking_table")
    suspend fun deleteAllEpisodes()

    @Query("DELETE FROM series_progress_table")
    suspend fun deleteAllProgress()

    @Query("DELETE FROM watch_list_table")
    suspend fun deleteAllWatchList()

    @Query("DELETE FROM favorites_movies_table")
    suspend fun deleteAllFavorites()

    @Query("DELETE FROM scheduled_movies_table")
    suspend fun deleteAllScheduled()
}

data class TrackedSeriesWithProgress(
    val id: Int,
    val name: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val lastUpdated: Long,
    val syncStatus: String,
    val lastWatchedEpisodeId: Int?,
    val lastWatchedSeasonNumber: Int?,
    val lastWatchedEpisodeNumber: Int?
)

data class SeasonWithProgress(
    val id: Int,
    val seriesId: Int,
    val seasonNumber: Int,
    val name: String?,
    val episodeCount: Int?,
    val posterPath: String?,
    val watchedCount: Int
)
