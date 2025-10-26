package com.example.movieflix.data.local_storage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movieflix.data.local_storage.entity.FavouritesEntity
import com.example.movieflix.data.local_storage.entity.HomeFeedEntity
import com.example.movieflix.data.local_storage.entity.WatchListEntity

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
    @Delete
    suspend fun deleteWatchListData(watchListEntity: WatchListEntity)
    @Query(" SELECT * FROM watch_list_table ORDER BY id DESC ")
    fun getAllWatchListData():LiveData<List<WatchListEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavMovie(favouritesEntity: FavouritesEntity)
    @Delete
    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)
    @Query( " SELECT * FROM favorites_movies_table ORDER BY id DESC ")
    fun getAllFavMovies():LiveData<List<FavouritesEntity>>
    @Query("UPDATE favorites_movies_table SET personalNote = :personalNote WHERE id = :favoriteId")
    suspend fun addPersonalNote(favoriteId: Int, personalNote: String?)

}