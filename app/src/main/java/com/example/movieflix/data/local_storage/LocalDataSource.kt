package com.example.movieflix.data.local_storage

import androidx.lifecycle.LiveData
import com.example.movieflix.data.local_storage.entity.FavouritesEntity
import com.example.movieflix.data.local_storage.entity.HomeFeedEntity
import com.example.movieflix.data.local_storage.entity.WatchListEntity
import com.example.movieflix.data.local_storage.entity.ScheduledEntity
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

    suspend fun addPersonalNote(favoriteId: Int, personalNote: String?) {
        return movieDao.addPersonalNote(favoriteId, personalNote)
    }

    suspend fun insertScheduledMovie(scheduledEntity: ScheduledEntity) {
        movieDao.insertScheduledMovie(scheduledEntity)
    }

    suspend fun deleteScheduledMovie(scheduledEntity: ScheduledEntity) {
        movieDao.deleteScheduledMovie(scheduledEntity)
    }

    fun getAllScheduledMovies(): LiveData<List<ScheduledEntity>> {
        return movieDao.getAllScheduledMovies()
    }

    suspend fun getScheduledMovieById(movieId: Int): ScheduledEntity? {
        return movieDao.getScheduledMovieById(movieId)
    }
}