package com.example.movieflix.data.repository

import android.app.Application
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.data.remote.RemoteDataSource
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.repository.SearchMovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

class SearchMovieMovieRepositoryImpl(private val appContext:Application, private val remoteDataSource: RemoteDataSource):SearchMovieRepository {
    override fun searchMovie(query: String): Flow<NetworkResults<MovieList>> = flow{
        emit(NetworkResults.Loading())

        try {
            if (isNetworkAvailable(appContext)){
                val response = remoteDataSource.getSearchResult(query)
            emit(NetworkResults.Success(response.body()?.toMovieList()))
            }
        }catch (e:Exception){

            when(e){

                is IOException->{
                    emit(NetworkResults.Error("Check ur internet connection"))
                }

                else->{
                    emit(NetworkResults.Error(e.message?:"Unknown error"))
                }
            }

        }

    }

    override fun trendingMovies(): Flow<NetworkResults<MovieList>> = flow{

try {
   if(isNetworkAvailable(appContext)){
       emit(NetworkResults.Loading())
       val response = remoteDataSource.getTrendingMovies()
       emit(NetworkResults.Success(response.body()?.toMovieList()))
   }
}catch (e:Exception){
    when(e){

        is IOException->{
            emit(NetworkResults.Error("Check ur internet connection"))
        }

        else->{
            emit(NetworkResults.Error(e.message?:"Unknown error"))
        }
    }
}

    }

}