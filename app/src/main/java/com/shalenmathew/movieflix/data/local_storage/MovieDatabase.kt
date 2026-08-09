package com.shalenmathew.movieflix.data.local_storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.HomeFeedEntity
import com.shalenmathew.movieflix.data.local_storage.entity.WatchListEntity
import com.shalenmathew.movieflix.data.local_storage.entity.ScheduledEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeriesTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.SeasonTrackingEntity
import com.shalenmathew.movieflix.data.local_storage.entity.EpisodeTrackingEntity

@Database(
    entities = [
        HomeFeedEntity::class,
        WatchListEntity::class,
        FavouritesEntity::class,
        ScheduledEntity::class,
        SeriesTrackingEntity::class,
        SeasonTrackingEntity::class,
        EpisodeTrackingEntity::class,
        com.shalenmathew.movieflix.data.local_storage.entity.SeriesProgressEntity::class
    ],
    version = 12
)
@TypeConverters(MovieDataTypeConverter::class)
abstract class MovieDatabase : RoomDatabase() {

    abstract val dao: MovieDao
    abstract val seriesTrackingDao: SeriesTrackingDao
}