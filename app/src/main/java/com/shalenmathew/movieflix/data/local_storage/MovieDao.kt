package com.shalenmathew.movieflix.data.local_storage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.HomeFeedEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeFeedData(homeFeedEntity: HomeFeedEntity)

    @Query(" SELECT * FROM movie_data_table ORDER BY id ASC ")
    suspend fun readHomeFeedData(): HomeFeedEntity

    @Query(" DELETE FROM movie_data_table ")
    suspend fun deleteHomeFeedData()


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchListData(watchListEntity: WatchListEntity)

    @Insert(entity = WatchListEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchListData(idAndMovieResult: IdAndMovieResult)

    @Delete
    suspend fun deleteWatchListData(watchListEntity: WatchListEntity)

    @Query(" SELECT * FROM watch_list_table ORDER BY id DESC ")
    fun getAllWatchListData(): LiveData<List<WatchListEntity>>

    @Query(" SELECT * FROM watch_list_table ")
    suspend fun getAllWatchListDataSync(): List<WatchListEntity>

    @Query("SELECT COUNT(*) > 0 FROM watch_list_table WHERE id = :id")
    suspend fun isMovieInWatchList(id: Int): Boolean

    @Query("SELECT * FROM watch_list_table WHERE id = :id")
    suspend fun getWatchListMovieById(id: Int): WatchListEntity?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavMovie(favouritesEntity: FavouritesEntity)

    @Insert(entity = FavouritesEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult)

    @Delete
    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)

    @Query(" SELECT * FROM favorites_movies_table ORDER BY id DESC ")
    fun getAllFavMovies(): LiveData<List<FavouritesEntity>>

    @Query(" SELECT * FROM favorites_movies_table ")
    suspend fun getAllFavMoviesSync(): List<FavouritesEntity>

    @Query("SELECT COUNT(*) > 0 FROM favorites_movies_table WHERE id = :id")
    suspend fun isMovieInFavorites(id: Int): Boolean

    @Query("SELECT * FROM favorites_movies_table WHERE id = :id")
    suspend fun getFavMovieById(id: Int): FavouritesEntity?

    @Query("UPDATE favorites_movies_table SET personalNote = :personalNote WHERE id = :favoriteId")
    suspend fun addPersonalNote(favoriteId: Int, personalNote: String?)

    @Query("UPDATE favorites_movies_table SET movieResult = :movieResult WHERE id = :id")
    suspend fun updateFavMovieResult(id: Int, movieResult: com.shalenmathew.movieflix.domain.model.MovieResult)

    @Query("UPDATE watch_list_table SET movieResult = :movieResult WHERE id = :id")
    suspend fun updateWatchListMovieResult(id: Int, movieResult: com.shalenmathew.movieflix.domain.model.MovieResult)

    @Query("UPDATE favorites_movies_table SET movieResult = :movieResult WHERE id = :id")
    suspend fun updateFavMovieBanner(id: Int, movieResult: com.shalenmathew.movieflix.domain.model.MovieResult)

    @Query("UPDATE watch_list_table SET movieResult = :movieResult WHERE id = :id")
    suspend fun updateWatchListMovieBanner(id: Int, movieResult: com.shalenmathew.movieflix.domain.model.MovieResult)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavMovies(favorites: List<FavouritesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchListItems(watchList: List<WatchListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMovies(scheduled: List<ScheduledEntity>)

    @Delete
    suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity)
    @Query(" SELECT * FROM scheduled_movies_table ORDER BY scheduledDate ASC ")
    fun getAllScheduledMovies():LiveData<List<ScheduledEntity>>

    @Query(" SELECT * FROM scheduled_movies_table ")
    suspend fun getAllScheduledMoviesSync(): List<ScheduledEntity>
    @Query("SELECT * FROM scheduled_movies_table WHERE id = :movieId LIMIT 1")
    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity?

}