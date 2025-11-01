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
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.MovieDao
import com.shalenmathew.movieflix.data.local_storage.MovieDataTypeConverter
import com.shalenmathew.movieflix.data.local_storage.MovieDatabase
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.data.repository.ActorRepositoryImpl
import com.shalenmathew.movieflix.data.repository.FavMovieRepositoryImpl
import com.shalenmathew.movieflix.data.repository.MovieDetailsRepositoryImpl
import com.shalenmathew.movieflix.data.repository.ScheduledRepositoryImpl
import com.shalenmathew.movieflix.data.repository.WatchListRepositoryImpl
import com.shalenmathew.movieflix.domain.repository.ActorRepository
import com.shalenmathew.movieflix.domain.repository.FavMovieRepository
import com.shalenmathew.movieflix.domain.repository.MovieInfoRepository
import com.shalenmathew.movieflix.domain.repository.ScheduledRepository
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
    fun providesMovieInfoRepositoryImpl(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        application: Application
    ): MovieInfoRepository {
        return MovieDetailsRepositoryImpl(remoteDataSource, localDataSource, application)
    }

    @Provides
    @Singleton
    fun getWatchList(localDataSource: LocalDataSource): WatchListRepository {
        return WatchListRepositoryImpl(localDataSource)
    }

    @Provides
    @Singleton
    fun getFavMovie(localDataSource: LocalDataSource): FavMovieRepository {
        return FavMovieRepositoryImpl(localDataSource)
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
    fun provideMovieScheduler(@ApplicationContext context: Context): MovieScheduler {
        return MovieScheduler(context)
    }

}