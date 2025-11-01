package com.shalenmathew.movieflix.data.local_storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.HomeFeedEntity
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity

@Database(
    entities = [HomeFeedEntity::class, WatchListEntity::class, FavouritesEntity::class, ScheduledEntity::class],
    version = 7
)
@TypeConverters(MovieDataTypeConverter::class)
abstract class MovieDatabase : RoomDatabase() {

    abstract val dao: MovieDao
}