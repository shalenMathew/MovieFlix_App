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


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavMovie(favouritesEntity: FavouritesEntity)

    @Insert(entity = FavouritesEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult)

    @Delete
    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)

    @Query(" SELECT * FROM favorites_movies_table ORDER BY id DESC ")
    fun getAllFavMovies(): LiveData<List<FavouritesEntity>>

    @Query("UPDATE favorites_movies_table SET personalNote = :personalNote WHERE id = :favoriteId")
    suspend fun addPersonalNote(favoriteId: Int, personalNote: String?)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity)
    @Delete
    suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity)
    @Query(" SELECT * FROM scheduled_movies_table ORDER BY scheduledDate ASC ")
    fun getAllScheduledMovies():LiveData<List<ScheduledEntity>>
    @Query("SELECT * FROM scheduled_movies_table WHERE id = :movieId LIMIT 1")
    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity?

}