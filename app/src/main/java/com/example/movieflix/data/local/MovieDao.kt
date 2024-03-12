package com.example.movieflix.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movieflix.data.local.entity.HomeFeedEntity
import com.example.movieflix.data.local.entity.WatchListEntity
import com.example.movieflix.domain.model.MovieResult

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
    @Query(" SELECT * FROM watch_list_news_table ORDER BY id DESC ")
 fun getAllWatchListData():LiveData<List<WatchListEntity>>

}