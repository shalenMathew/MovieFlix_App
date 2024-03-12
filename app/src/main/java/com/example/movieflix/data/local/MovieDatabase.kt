package com.example.movieflix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.movieflix.data.local.entity.HomeFeedEntity
import com.example.movieflix.data.local.entity.WatchListEntity

@Database(entities = [HomeFeedEntity::class,WatchListEntity::class], version = 2)
@TypeConverters(MovieDataTypeConverter::class)
 abstract class MovieDatabase:RoomDatabase(){
abstract val dao:MovieDao
}