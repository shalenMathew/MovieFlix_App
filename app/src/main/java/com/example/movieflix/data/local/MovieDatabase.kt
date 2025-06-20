package com.example.movieflix.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.example.movieflix.data.local.entity.FavouritesEntity
import com.example.movieflix.data.local.entity.HomeFeedEntity
import com.example.movieflix.data.local.entity.WatchListEntity

@Database(entities = [HomeFeedEntity::class,WatchListEntity::class,FavouritesEntity::class],
 version = 4)
@TypeConverters(MovieDataTypeConverter::class)
 abstract class MovieDatabase:RoomDatabase(){

  abstract val dao:MovieDao
}