package com.example.movieflix.data.local

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local.entity.FavouritesEntity
import com.example.movieflix.data.local.entity.HomeFeedEntity
import com.example.movieflix.data.local.entity.WatchListEntity
import com.example.movieflix.domain.model.MovieResult
import javax.inject.Inject

class LocalDataSource @Inject constructor(private val movieDao: MovieDao) {
suspend fun insertHomeFeedData(homeFeedEntity: HomeFeedEntity){
    movieDao.insertHomeFeedData(homeFeedEntity)
}
  suspend  fun readHomeFeedData():HomeFeedEntity{
        return movieDao.readHomeFeedData()
    }
   suspend fun deleteAllHomeFeedData(){
        movieDao.deleteHomeFeedData()
    }

    suspend fun insertWatchListData(watchListEntity: WatchListEntity){
        movieDao.insertWatchListData(watchListEntity)
    }

    suspend fun deleteWatchListData(watchListEntity: WatchListEntity){
        movieDao.deleteWatchListData(watchListEntity)
    }

      fun getAllWatchListData():LiveData<List<WatchListEntity>>{
        return movieDao.getAllWatchListData()
    }

    suspend fun insertFavMovie(favouritesEntity: FavouritesEntity){
        movieDao.insertFavMovie(favouritesEntity)
    }

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity){
        movieDao.deleteFavMovie(favouritesEntity)
    }

     fun getAllFavMovie():LiveData<List<FavouritesEntity>>{
         return movieDao.getAllFavMovies()
     }

}