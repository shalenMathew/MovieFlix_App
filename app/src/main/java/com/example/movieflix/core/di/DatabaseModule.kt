package com.example.movieflix.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.GsonParser
import com.example.movieflix.data.local.LocalDataSource
import com.example.movieflix.data.local.MovieDao
import com.example.movieflix.data.local.MovieDataTypeConverter
import com.example.movieflix.data.local.MovieDatabase
import com.example.movieflix.data.remote.RemoteDataSource
import com.example.movieflix.data.repository.MovieInfoRepositoryImpl
import com.example.movieflix.data.repository.WatchListRepositoryImpl
import com.example.movieflix.domain.repository.MovieInfoRepository
import com.example.movieflix.domain.repository.WatchListRepository
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
        return Room.databaseBuilder(context,MovieDatabase::class.java,Constants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addTypeConverter(MovieDataTypeConverter(GsonParser(Gson())))
            .build()
    }

    @Provides
    @Singleton
    fun  providesMovieDao(movieDatabase: MovieDatabase):MovieDao{
        return  movieDatabase.dao
    }

    @Provides
    @Singleton
    fun providesMovieInfoRepositoryImpl(
        remoteDataSource: RemoteDataSource,
        localDataSource: LocalDataSource,
        application: Application
    ): MovieInfoRepository {
        return MovieInfoRepositoryImpl(remoteDataSource,localDataSource,application)
    }

    @Provides
    @Singleton
    fun getWatchList(localDataSource: LocalDataSource):WatchListRepository{
        return WatchListRepositoryImpl(localDataSource)
    }

}