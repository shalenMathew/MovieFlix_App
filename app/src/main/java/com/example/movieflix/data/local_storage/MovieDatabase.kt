package com.example.movieflix.data.local_storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.movieflix.data.local_storage.entity.FavouritesEntity
import com.example.movieflix.data.local_storage.entity.HomeFeedEntity
import com.example.movieflix.data.local_storage.entity.WatchListEntity

@Database(
    entities = [HomeFeedEntity::class, WatchListEntity::class, FavouritesEntity::class],
    version = 5
)
@TypeConverters(MovieDataTypeConverter::class)
 abstract class MovieDatabase:RoomDatabase(){

  abstract val dao:MovieDao
}