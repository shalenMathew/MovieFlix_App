package com.example.movieflix.data.repository

import android.app.Application
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.data.local_storage.LocalDataSource
import com.example.movieflix.data.local_storage.entity.HomeFeedEntity
import com.example.movieflix.data.model.HomeFeedDataResponse
import com.example.movieflix.data.model.HomeFeedResponse
import com.example.movieflix.data.remote.RemoteDataSource
import com.example.movieflix.domain.model.CastMember
import com.example.movieflix.domain.model.CrewMember
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.model.MediaVideoResultList
import com.example.movieflix.domain.model.WatchProviders
import com.example.movieflix.domain.repository.MovieInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

class MovieDetailsRepositoryImpl(
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
                    val trendingMovieListDef =  async { remoteDataSource.getTrendingMovies() }
                    val popularMovieListDef =  async { remoteDataSource.getPopularMovies() }
                    val topRatedTVListDef =  async { remoteDataSource.getTopRatedTV()}
                    val netflixShowListDef =  async { remoteDataSource.getNetflixShows()}
                    val amazonPrimeShowListDef = async { remoteDataSource.getAmazonPrimeShows() }
                    val bollywoodMovieListDef =  async { remoteDataSource.getBollywoodMovies() }
                    val movieBannerOnHomeListDef = async { remoteDataSource.getNowPlayingMovies() }


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
                    val movieBannerOnHomeList = movieBannerOnHomeListDef.await()

                    wholeMoviesList.add(HomeFeedResponse(Constants.UPCOMING_MOVIES, upcomingMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.TRENDING_MOVIES,trendingMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.POPULAR_MOVIES,popularMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.TOP_RATED_MOVIES,topRatedMovieList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.NETFLIX_SHOWS,netflixShowList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.PRIME_SHOWS,amazonPrimeShowList.body()?.results!!))
                    wholeMoviesList.add(HomeFeedResponse(Constants.BOLLYWOOD_MOVIES,bollywoodMoviesList.body()?.results!!))

//                     homeFeedResponse = HomeFeedDataResponse(nowPlayingMoviesList.body()?.results!!,wholeMoviesList) // this line of code only makes the
//                     response from api to work only if there is an internet connection, so below we using local data storage to make
//                     life little easier

                    localDataSource.deleteAllHomeFeedData() // this for offline support
                    localDataSource.insertHomeFeedData(HomeFeedEntity( // here we r storing whatever data fetched from  online in database
                        bannerMovies = movieBannerOnHomeList.body()?.results!!,
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

    override fun getMovieTrailer(movieId:Int): Flow<NetworkResults<MediaVideoResultList>> = flow {

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

    override fun getTVTrailer(tvId: Int): Flow<NetworkResults<MediaVideoResultList>> = flow {

        emit(NetworkResults.Loading())

        try{
            if(isNetworkAvailable(appContext)){
                val apiResponse = remoteDataSource.getTVTrailer(tvId)
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

    override fun loadMoreMoviesForCategory(categoryTitle: String, page: Int): Flow<NetworkResults<MovieList>> = flow {
        emit(NetworkResults.Loading())

        try {
            if (isNetworkAvailable(appContext)) {
                val response = when (categoryTitle) {
                    Constants.UPCOMING_MOVIES -> remoteDataSource.getUpcomingMovies(page)
                    Constants.TRENDING_MOVIES -> remoteDataSource.getTrendingMovies(page)
                    Constants.POPULAR_MOVIES -> remoteDataSource.getPopularMovies(page)
                    Constants.TOP_RATED_MOVIES -> remoteDataSource.getTopRatedTV(page)
                    Constants.BOLLYWOOD_MOVIES -> remoteDataSource.getBollywoodMovies(page)
                    Constants.NOW_PLAYING_MOVIES -> remoteDataSource.getNowPlayingMovies(page)
                    Constants.NETFLIX_SHOWS -> remoteDataSource.getNetflixShows(page)
                    Constants.PRIME_SHOWS -> remoteDataSource.getAmazonPrimeShows(page)
                    else -> null
                }

                response?.let {
                    emit(NetworkResults.Success(it.body()?.toMovieList()))
                } ?: emit(NetworkResults.Error("Unknown category"))
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    emit(NetworkResults.Error("Check ur internet connection"))
                }
                else -> {
                    emit(NetworkResults.Error(e.message ?: "Unknown error"))
                }
            }
        }
    }

    override fun getMovieCast(movieId: Int): Flow<NetworkResults<List<CastMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (isNetworkAvailable(appContext)) {
                val castResponse = remoteDataSource.getMovieCast(movieId)
                castResponse.body()?.let { response ->
                    val castList = response.cast?.take(10)?.mapNotNull { cast ->
                        cast.id?.let { personId ->
                            CastMember(
                                id = personId,
                                name = cast.name ?: "",
                                character = cast.character ?: "",
                                profilePath = cast.profilePath
                            )
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(castList))
                } ?: emit(NetworkResults.Error("No cast data available"))
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error("Check ur internet connection"))
                else -> emit(NetworkResults.Error(e.message ?: "Unknown error"))
            }
        }
    }

    override fun getTVCast(tvId: Int): Flow<NetworkResults<List<CastMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (isNetworkAvailable(appContext)) {
                val castResponse = remoteDataSource.getTVCast(tvId)
                castResponse.body()?.let { response ->
                    val castList = response.cast?.take(10)?.mapNotNull { cast ->
                        cast.id?.let { personId ->
                            CastMember(
                                id = personId,
                                name = cast.name ?: "",
                                character = cast.character ?: "",
                                profilePath = cast.profilePath
                            )
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(castList))
                } ?: emit(NetworkResults.Error("No cast data available"))
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error("Check ur internet connection"))
                else -> emit(NetworkResults.Error(e.message ?: "Unknown error"))
            }
        }
    }

    override fun getMovieCrew(movieId: Int): Flow<NetworkResults<List<CrewMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (isNetworkAvailable(appContext)) {
                val castResponse = remoteDataSource.getMovieCast(movieId)
                castResponse.body()?.let { response ->
                    // Filter for Director, Writer, and Producer
                    val crewList = response.crew?.filter { crew ->
                        crew.job in listOf("Director", "Writer", "Screenplay", "Producer", "Executive Producer")
                    }?.distinctBy { it.id }?.mapNotNull { crew ->
                        crew.id?.let { personId ->
                            CrewMember(
                                id = personId,
                                name = crew.name ?: "",
                                job = crew.job ?: "",
                                profilePath = crew.profilePath
                            )
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(crewList))
                } ?: emit(NetworkResults.Error("No crew data available"))
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error("Check ur internet connection"))
                else -> emit(NetworkResults.Error(e.message ?: "Unknown error"))
            }
        }
    }

    override fun getTVCrew(tvId: Int): Flow<NetworkResults<List<CrewMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (isNetworkAvailable(appContext)) {
                val castResponse = remoteDataSource.getTVCast(tvId)
                castResponse.body()?.let { response ->
                    // Filter for Director, Writer, and Producer
                    val crewList = response.crew?.filter { crew ->
                        crew.job in listOf("Director", "Writer", "Screenplay", "Producer", "Executive Producer")
                    }?.distinctBy { it.id }?.mapNotNull { crew ->
                        crew.id?.let { personId ->
                            CrewMember(
                                id = personId,
                                name = crew.name ?: "",
                                job = crew.job ?: "",
                                profilePath = crew.profilePath
                            )
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(crewList))
                } ?: emit(NetworkResults.Error("No crew data available"))
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error("Check ur internet connection"))
                else -> emit(NetworkResults.Error(e.message ?: "Unknown error"))
            }
        }
    }

}

