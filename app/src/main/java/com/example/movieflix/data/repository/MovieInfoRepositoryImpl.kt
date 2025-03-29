package com.example.movieflix.data.repository

import android.app.Application
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.data.local.LocalDataSource
import com.example.movieflix.data.local.entity.HomeFeedEntity
import com.example.movieflix.data.model.HomeFeedDataResponse
import com.example.movieflix.data.model.HomeFeedResponse
import com.example.movieflix.data.remote.RemoteDataSource
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.model.MovieVideoResultList
import com.example.movieflix.domain.model.WatchProviders
import com.example.movieflix.domain.repository.MovieInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

class MovieInfoRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val appContext:Application
):MovieInfoRepository {
    private lateinit var  homeFeedResponse: HomeFeedDataResponse
    override fun getHomeFeedData(): Flow<NetworkResults<HomeFeedData>> = flow{


        emit(NetworkResults.Loading())

        try {
            if (isNetworkAvailable(appContext)){

                withContext(Dispatchers.IO){
                    // async returns a Deferred object, which represents a future result12. This means the result of the async
                    // task isn’t immediately available,
                    // but will be at some point in the future

                    val upcomingMovieListDef = async { remoteDataSource.getUpcomingMovies() }
                    val popularMovieListDef =  async { remoteDataSource.getPopularMovies() }
                    val trendingMovieListDef =  async { remoteDataSource.getTrendingMovies() }
                    val topRatedTVListDef =  async { remoteDataSource.getTopRatedTV()}
                    val netflixShowListDef =  async { remoteDataSource.getNetflixShows()}
                    val amazonPrimeShowListDef = async { remoteDataSource.getAmazonPrimeShows() }
                    val bollywoodMovieListDef =  async { remoteDataSource.getBollywoodMovies() }
                    val nowPlayingMoviesListDef = async { remoteDataSource.getNowPlayingMovies() }

//                    val wholeMovieList = mutableListOf<HomeFeed>() can't directly Fetch HomeFeed as its in domain layer which will
//                    break the rule of clean architecture so instead we have to make HomeResponse class in data layer which has the
//                    responsibility to
                    // convert the data to HomeFeed

                    // If a data layer class or function is directly using a domain layer class (like HomeFeed), it could be seen as a violation of
                // the principles of Clean Architecture. This is because it creates a direct dependency from the data layer to the domain layer,
                // which can make the code harder to maintain and test.
                    //
                    //Typically, the data layer should only deal with data models (like HomeFeedResponse). Then, a separate mapper or
                // transformer function (which could be defined in the domain layer or as a separate utility) should be responsible
                // for converting these data models into domain models (like HomeFeed).
                    //
                    //This ensures that each layer only knows about its own models and doesn’t depend directly on the models from
                // other layers. It also makes it easier to change or swap out implementations in one layer without affecting the others.

                    val wholeMoviesList= mutableListOf<HomeFeedResponse>()
                    val upcomingMovieList = upcomingMovieListDef.await()
                    val popularMovieList = popularMovieListDef.await()
                    val trendingMovieList= trendingMovieListDef.await()
                    val topRatedMovieList = topRatedTVListDef.await()
                    val netflixShowList = netflixShowListDef.await()
                    val amazonPrimeShowList = amazonPrimeShowListDef.await()
                    val bollywoodMoviesList = bollywoodMovieListDef.await()
                    val nowPlayingMoviesList = nowPlayingMoviesListDef.await()

                    wholeMoviesList.add(HomeFeedResponse(
                        Constants.UPCOMING_MOVIES,
                        upcomingMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.POPULAR_MOVIES,popularMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.TRENDING_MOVIES,trendingMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.TOP_RATED_MOVIES,topRatedMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.NETFLIX_SHOWS,netflixShowList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.PRIME_SHOWS,amazonPrimeShowList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.BOLLYWOOD_MOVIES,bollywoodMoviesList.body()?.results!!))

//                     homeFeedResponse = HomeFeedDataResponse(nowPlayingMoviesList.body()?.results!!,wholeMoviesList) // this is for just on line support

                    localDataSource.deleteAllHomeFeedData() // this for offline support
                    localDataSource.insertHomeFeedData(HomeFeedEntity( // here we r storing whatever data fetched from  online in database
                        bannerMovies = nowPlayingMoviesList.body()?.results!!,
                        homeFeedResponseList = wholeMoviesList))
                }

            }

            val data = localDataSource.readHomeFeedData().toHomeFeedData()
            emit(NetworkResults.Success(data))

        }catch (e:Exception){
            when(e){

                is IOException -> {
                    emit(NetworkResults.Error("Check ur internet connection"))
                }

                else ->{
                    emit(NetworkResults.Error(e.message ?: "Something went wrong"))
                }
            }
        }
    }
    override fun getMovieTrailer(movieId:Int): Flow<NetworkResults<MovieVideoResultList>> = flow {

        emit(NetworkResults.Loading())

        try{
            if(isNetworkAvailable(appContext)){
                val apiResponse = remoteDataSource.getMovieTrailer(movieId)
                emit(NetworkResults.Success(apiResponse.body()?.toMovieVideoResultList()))
            }
        }catch (e:Throwable){

            when(e){
                is IOException -> emit(NetworkResults.Error("Please check ur internet"))

                else -> {
                    emit(NetworkResults.Error(e.message?:"Something went wrong"))
                }
            }
        }
    }
    override fun getRecommendation(movieId: Int): Flow<NetworkResults<MovieList>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (isNetworkAvailable(appContext)){

                val response = remoteDataSource.getRecommendation(movieId)
                emit(NetworkResults.Success(response.body()?.toMovieList()))
            }
        }catch (e:Exception){

            when(e){
                is IOException -> {
                    emit(NetworkResults.Error("check ur internet connection"))
                }
                else->{
                   emit(NetworkResults.Error(e.message?:"Unknown error"))
                }
            }
        }

    }
    override fun getWhereToWatchProvider(movieId: Int): Flow<NetworkResults<WatchProviders>> =flow{

      try {
          if (isNetworkAvailable(appContext)){
              val whereToWatchResponse = remoteDataSource.getWhereToWatchProviders(movieId)
              emit(NetworkResults.Success(whereToWatchResponse.body()?.toWatchProviders()))
          }
      }catch (e:Exception){
          when(e){
              is IOException -> {
                  emit(NetworkResults.Error("Check ur internet connection"))
              }
              else -> {
                  emit(NetworkResults.Error(e.message?:"Unknown error"))
              }
          }
      }
    }


}

