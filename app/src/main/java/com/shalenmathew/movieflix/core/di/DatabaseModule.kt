package com.shalenmathew.movieflix.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.GsonParser
import com.shalenmathew.movieflix.core.utils.MIGRATION_3_4
import com.shalenmathew.movieflix.core.utils.MIGRATION_4_5
import com.shalenmathew.movieflix.core.utils.MIGRATION_6_7
import com.shalenmathew.movieflix.core.utils.MIGRATION_5_6
import com.shalenmathew.movieflix.core.utils.MIGRATION_7_8
import com.shalenmathew.movieflix.core.utils.MIGRATION_8_9
import com.shalenmathew.movieflix.core.utils.MIGRATION_9_10
import com.shalenmathew.movieflix.core.utils.MIGRATION_10_11
import com.shalenmathew.movieflix.core.utils.MIGRATION_11_12
import com.shalenmathew.movieflix.core.utils.MIGRATION_12_13
import com.shalenmathew.movieflix.core.utils.MIGRATION_13_14
import com.shalenmathew.movieflix.core.utils.MIGRATION_14_15
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.MovieDao
import com.shalenmathew.movieflix.data.local_storage.MovieDataTypeConverter
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import com.shalenmathew.movieflix.data.local_storage.SeriesTrackingDao
import com.shalenmathew.movieflix.data.local_storage.CustomListDao
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.data.repository.ActorRepositoryImpl
import com.shalenmathew.movieflix.data.repository.FavMovieRepositoryImpl
import com.shalenmathew.movieflix.data.repository.MovieDetailsRepositoryImpl
import com.shalenmathew.movieflix.data.repository.ScheduledRepositoryImpl
import com.shalenmathew.movieflix.data.repository.SeriesTrackingRepositoryImpl
import com.shalenmathew.movieflix.data.repository.CustomListRepositoryImpl
import com.shalenmathew.movieflix.data.repository.WatchListRepositoryImpl
import com.shalenmathew.movieflix.domain.repository.ActorRepository
import com.shalenmathew.movieflix.domain.repository.FavMovieRepository
import com.shalenmathew.movieflix.domain.repository.MovieInfoRepository
import com.shalenmathew.movieflix.domain.repository.ScheduledRepository
import com.shalenmathew.movieflix.domain.repository.SeriesTrackingRepository
import com.shalenmathew.movieflix.domain.repository.CustomListRepository
import com.shalenmathew.movieflix.domain.repository.WatchListRepository
import com.shalenmathew.movieflix.core.notifications.MovieScheduler
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun providesMovieDataBase(@ApplicationContext context: Context): MovieDatabase {
        return Room.databaseBuilder(context, MovieDatabase::class.java, Constants.DATABASE_NAME)
//            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .addMigrations(
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15
            )
            .addTypeConverter(MovieDataTypeConverter(GsonParser(Gson())))
            .build()
    }

    @Provides
    @Singleton
    fun providesMovieDao(movieDatabase: MovieDatabase): MovieDao {
        return movieDatabase.dao
    }

    @Provides
    @Singleton
    fun providesSeriesTrackingDao(movieDatabase: MovieDatabase): SeriesTrackingDao {
        return movieDatabase.seriesTrackingDao
    }

    @Provides
    @Singleton
    fun providesCustomListDao(movieDatabase: MovieDatabase): CustomListDao {
        return movieDatabase.customListDao
    }

    @Provides
    @Singleton
    fun providesMovieInfoRepositoryImpl(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        seriesTrackingDao: SeriesTrackingDao,
        application: Application
    ): MovieInfoRepository {
        return MovieDetailsRepositoryImpl(remoteDataSource, localDataSource, seriesTrackingDao, application)
    }

    @Provides
    @Singleton
    fun getWatchList(
        localDataSource: LocalDataSource,
        seriesTrackingRepository: SeriesTrackingRepository
    ): WatchListRepository {
        return WatchListRepositoryImpl(localDataSource, seriesTrackingRepository)
    }

    @Provides
    @Singleton
    fun getFavMovie(
        localDataSource: LocalDataSource,
        seriesTrackingRepository: SeriesTrackingRepository
    ): FavMovieRepository {
        return FavMovieRepositoryImpl(localDataSource, seriesTrackingRepository)
    }

    @Provides
    @Singleton
    fun providesActorRepository(
        remoteDataSource: RemoteDataSource,
        application: Application
    ): ActorRepository {
        return ActorRepositoryImpl(remoteDataSource, application)
    }

    @Provides
    @Singleton
    fun getScheduled(localDataSource: LocalDataSource): ScheduledRepository {
        return ScheduledRepositoryImpl(localDataSource)
    }

    @Provides
    @Singleton
    fun providesSeriesTrackingRepository(
        seriesTrackingDao: SeriesTrackingDao,
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        @ApplicationContext context: Context
    ): SeriesTrackingRepository {
        return SeriesTrackingRepositoryImpl(seriesTrackingDao, remoteDataSource, localDataSource, context)
    }

    @Provides
    @Singleton
    fun provideMovieScheduler(@ApplicationContext context: Context): MovieScheduler {
        return MovieScheduler(context)
    }

    @Provides
    @Singleton
    fun providesCustomListRepository(
        customListDao: CustomListDao
    ): CustomListRepository {
        return CustomListRepositoryImpl(customListDao)
    }

}