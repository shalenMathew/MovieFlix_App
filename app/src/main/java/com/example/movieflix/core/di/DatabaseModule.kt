package com.example.movieflix.core.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.GsonParser
import com.example.movieflix.core.utils.MIGRATION
import com.example.movieflix.data.local_storage.LocalDataSource
import com.example.movieflix.data.local_storage.MovieDao
import com.example.movieflix.data.local_storage.MovieDataTypeConverter
import com.example.movieflix.data.local_storage.MovieDatabase
import com.example.movieflix.data.remote.RemoteDataSource
import com.example.movieflix.data.repository.ActorRepositoryImpl
import com.example.movieflix.data.repository.FavMovieRepositoryImpl
import com.example.movieflix.data.repository.MovieDetailsRepositoryImpl
import com.example.movieflix.data.repository.WatchListRepositoryImpl
import com.example.movieflix.domain.repository.ActorRepository
import com.example.movieflix.domain.repository.FavMovieRepository
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
//            .fallbackToDestructiveMigration()
            .addMigrations(MIGRATION)
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
        return MovieDetailsRepositoryImpl(remoteDataSource,localDataSource,application)
    }

    @Provides
    @Singleton
    fun getWatchList(localDataSource: LocalDataSource):WatchListRepository{
        return WatchListRepositoryImpl(localDataSource)
    }

    @Provides
    @Singleton
    fun getFavMovie(localDataSource: LocalDataSource):FavMovieRepository{
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

}