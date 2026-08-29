package com.shalenmathew.movieflix.data.repository

import android.app.Application
import android.content.Context
import com.shalenmathew.movieflix.R
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.core.utils.isNetworkAvailable
import com.shalenmathew.movieflix.data.local_storage.LocalDataSource
import com.shalenmathew.movieflix.data.local_storage.entity.HomeFeedEntity
import com.shalenmathew.movieflix.data.model.HomeFeedDataResponse
import com.shalenmathew.movieflix.data.model.HomeFeedResponse
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.domain.model.CastMember
import com.shalenmathew.movieflix.domain.model.CrewMember
import com.shalenmathew.movieflix.domain.model.HomeFeedData
import com.shalenmathew.movieflix.domain.model.MovieList
import com.shalenmathew.movieflix.domain.model.MediaVideoResultList
import com.shalenmathew.movieflix.domain.model.WatchProviders
import com.shalenmathew.movieflix.domain.repository.MovieInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

class MovieDetailsRepositoryImpl(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val seriesTrackingDao: com.shalenmathew.movieflix.data.local_storage.SeriesTrackingDao,
    private val appContext:Application,
    private val networkChecker: (Context?) -> Boolean = ::isNetworkAvailable
):MovieInfoRepository {

    private lateinit var  homeFeedResponse: HomeFeedDataResponse
    override fun getHomeFeedData(): Flow<NetworkResults<HomeFeedData>> = flow{

        emit(NetworkResults.Loading())

        try {
            if (networkChecker(appContext)){

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
                    val animeListDef = async { remoteDataSource.getAnime() }
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
                    val animeList = animeListDef.await()
                    val movieBannerOnHomeList = movieBannerOnHomeListDef.await()

                    upcomingMovieList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.UPCOMING_MOVIES, it)) 
                    }
                    trendingMovieList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.TRENDING_MOVIES, it)) 
                    }
                    popularMovieList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.POPULAR_MOVIES, it)) 
                    }
                    topRatedMovieList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.TOP_RATED_MOVIES, it)) 
                    }
                    netflixShowList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.NETFLIX_SHOWS, it)) 
                    }
                    amazonPrimeShowList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.PRIME_SHOWS, it)) 
                    }
                    animeList.body()?.results?.let { 
                        wholeMoviesList.add(HomeFeedResponse(Constants.ANIME_SERIES, it)) 
                    }

                    localDataSource.deleteAllHomeFeedData() 
                    localDataSource.insertHomeFeedData(HomeFeedEntity(
                        bannerMovies = movieBannerOnHomeList.body()?.results ?: emptyList(),
                        homeFeedResponseList = wholeMoviesList))
                }

            }

            val data = localDataSource.readHomeFeedData().toHomeFeedData()
            emit(NetworkResults.Success(data))

        }catch (e:Exception){
            when(e){

                is IOException -> {
                    emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                }

                else ->{
                    emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
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
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))

                else -> {
                    emit(NetworkResults.Error(e.message?:appContext.getString(R.string.msg_something_went_wrong)))
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
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))

                else -> {
                    emit(NetworkResults.Error(e.message?:appContext.getString(R.string.msg_something_went_wrong)))
                }
            }
        }
    }

    override fun getRecommendation(movieId: Int): Flow<NetworkResults<MovieList>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)){

                val response = remoteDataSource.getRecommendation(movieId)
                emit(NetworkResults.Success(response.body()?.toMovieList()))
            }
        }catch (e:Exception){

            when(e){
                is IOException -> {
                    emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                }
                else->{
                   emit(NetworkResults.Error(e.message?:appContext.getString(R.string.msg_something_went_wrong)))
                }
            }
        }

    }

    override fun getWhereToWatchProvider(movieId: Int): Flow<NetworkResults<WatchProviders>> =flow{

      try {
          if (networkChecker(appContext)){
              val whereToWatchResponse = remoteDataSource.getWhereToWatchProviders(movieId)
              emit(NetworkResults.Success(whereToWatchResponse.body()?.toWatchProviders()))
          }
      }catch (e:Exception){
          when(e){
              is IOException -> {
                  emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
              }
              else -> {
                  emit(NetworkResults.Error(e.message?:appContext.getString(R.string.msg_something_went_wrong)))
              }
          }
      }
    }

    override fun getTVWhereToWatchProvider(tvId: Int): Flow<NetworkResults<WatchProviders>> =flow{

      try {
          if (networkChecker(appContext)){
              val whereToWatchResponse = remoteDataSource.getTVWatchProviders(tvId)
              emit(NetworkResults.Success(whereToWatchResponse.body()?.toWatchProviders()))
          }
      }catch (e:Exception){
          when(e){
              is IOException -> {
                  emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
              }
              else -> {
                  emit(NetworkResults.Error(e.message?:appContext.getString(R.string.msg_something_went_wrong)))
              }
          }
      }
    }

    override fun loadMoreMoviesForCategory(categoryTitle: String, page: Int): Flow<NetworkResults<MovieList>> = flow {
        emit(NetworkResults.Loading())

        try {
            if (networkChecker(appContext)) {
                val response = when (categoryTitle) {
                    Constants.UPCOMING_MOVIES -> remoteDataSource.getUpcomingMovies(page)
                    Constants.TRENDING_MOVIES -> remoteDataSource.getTrendingMovies(page)
                    Constants.POPULAR_MOVIES -> remoteDataSource.getPopularMovies(page)
                    Constants.POPULAR_TV_SHOWS -> remoteDataSource.getTopRatedTV(page) // Assuming this for now or dedicated call
                    Constants.TOP_RATED_MOVIES -> remoteDataSource.getTopRatedTV(page)
                    Constants.ANIME_SERIES -> remoteDataSource.getAnime(page)
                    Constants.NOW_PLAYING_MOVIES -> remoteDataSource.getNowPlayingMovies(page)
                    Constants.NETFLIX_SHOWS -> remoteDataSource.getNetflixShows(page)
                    Constants.PRIME_SHOWS -> remoteDataSource.getAmazonPrimeShows(page)
                    else -> null
                }

                response?.let {
                    emit(NetworkResults.Success(it.body()?.toMovieList()))
                } ?: emit(NetworkResults.Error(appContext.getString(R.string.msg_unknown_category)))
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> {
                    emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                }
                else -> {
                    emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
                }
            }
        }
    }

    override fun getMovieCast(movieId: Int): Flow<NetworkResults<List<CastMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                val castResponse = remoteDataSource.getMovieCast(movieId)
                castResponse.body()?.let { response ->
                    val castList = response.cast?.take(10)?.mapNotNull { cast ->
                        // Only include cast with valid ID and name
                        if (cast.id != null && cast.id > 0 && !cast.name.isNullOrBlank()) {
                            CastMember(
                                id = cast.id,
                                name = cast.name,
                                character = cast.character?.takeIf { it.isNotBlank() } ?: "",
                                profilePath = cast.profilePath?.takeIf { it.isNotBlank() }
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(castList))
                } ?: emit(NetworkResults.Error(appContext.getString(R.string.msg_no_cast_available)))
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                else -> emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
            }
        }
    }

    override fun getTVCast(tvId: Int): Flow<NetworkResults<List<CastMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                val castResponse = remoteDataSource.getTVCast(tvId)
                castResponse.body()?.let { response ->
                    val castList = response.cast?.take(10)?.mapNotNull { cast ->
                        // Only include cast with valid ID and name
                        if (cast.id != null && cast.id > 0 && !cast.name.isNullOrBlank()) {
                            CastMember(
                                id = cast.id,
                                name = cast.name,
                                character = cast.character?.takeIf { it.isNotBlank() } ?: "",
                                profilePath = cast.profilePath?.takeIf { it.isNotBlank() }
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(castList))
                } ?: emit(NetworkResults.Error(appContext.getString(R.string.msg_no_cast_available)))
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                else -> emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
            }
        }
    }

    override fun getMovieCrew(movieId: Int): Flow<NetworkResults<List<CrewMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                val castResponse = remoteDataSource.getMovieCast(movieId)
                castResponse.body()?.let { response ->
                    // Filter for Director, Writer, and Producer
                    val crewList = response.crew?.filter { crew ->
                        crew.job in listOf("Director", "Writer", "Screenplay", "Producer", "Executive Producer")
                    }?.distinctBy { it.id }?.mapNotNull { crew ->
                        // Only include crew with valid ID and name
                        if (crew.id != null && crew.id > 0 && !crew.name.isNullOrBlank()) {
                            CrewMember(
                                id = crew.id,
                                name = crew.name,
                                job = crew.job ?: "",
                                profilePath = crew.profilePath?.takeIf { it.isNotBlank() }
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(crewList))
                } ?: emit(NetworkResults.Error(appContext.getString(R.string.msg_no_crew_available)))
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                else -> emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
            }
        }
    }

    override fun getTVCrew(tvId: Int): Flow<NetworkResults<List<CrewMember>>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                val castResponse = remoteDataSource.getTVCast(tvId)
                castResponse.body()?.let { response ->
                    // Filter for Director, Writer, and Producer
                    val crewList = response.crew?.filter { crew ->
                        crew.job in listOf("Director", "Writer", "Screenplay", "Producer", "Executive Producer")
                    }?.distinctBy { it.id }?.mapNotNull { crew ->
                        // Only include crew with valid ID and name
                        if (crew.id != null && crew.id > 0 && !crew.name.isNullOrBlank()) {
                            CrewMember(
                                id = crew.id,
                                name = crew.name,
                                job = crew.job ?: "",
                                profilePath = crew.profilePath?.takeIf { it.isNotBlank() }
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()
                    
                    emit(NetworkResults.Success(crewList))
                } ?: emit(NetworkResults.Error(appContext.getString(R.string.msg_no_crew_available)))
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                else -> emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
            }
        }
    }

    override fun getTVDetail(tvId: Int): Flow<NetworkResults<com.shalenmathew.movieflix.domain.model.TVDetail>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                val tvDetailResponse = remoteDataSource.getTVDetail(tvId)
                if (tvDetailResponse.isSuccessful) {
                    tvDetailResponse.body()?.let { response ->
                        emit(NetworkResults.Success(response.toTVDetail()))
                        return@flow
                    }
                }
            }

            // Offline or network failed
            val trackedSeries = seriesTrackingDao.getSeriesById(tvId)
            if (trackedSeries != null) {
                val seasons = seriesTrackingDao.getSeasonsForSeries(tvId).first()
                emit(NetworkResults.Success(com.shalenmathew.movieflix.domain.model.TVDetail(
                    id = trackedSeries.id,
                    name = trackedSeries.name,
                    overview = trackedSeries.overview,
                    posterPath = trackedSeries.posterPath,
                    backdropPath = trackedSeries.backdropPath,
                    voteAverage = null,
                    firstAirDate = null,
                    numberOfSeasons = seasons.size,
                    numberOfEpisodes = seasons.sumOf { it.episodeCount ?: 0 },
                    seasons = seasons.map { 
                        com.shalenmathew.movieflix.domain.model.TVSeasonBasic(
                            id = it.id,
                            airDate = null,
                            episodeCount = it.episodeCount,
                            name = it.name,
                            overview = null,
                            posterPath = it.posterPath,
                            seasonNumber = it.seasonNumber
                        )
                    }
                )))
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                else -> emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
            }
        }
    }

    override fun getTVSeason(tvId: Int, seasonNumber: Int): Flow<NetworkResults<com.shalenmathew.movieflix.domain.model.TVSeason>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                val seasonResponse = remoteDataSource.getTVSeason(tvId, seasonNumber)
                if (seasonResponse.isSuccessful) {
                    seasonResponse.body()?.let { response ->
                        val domainSeason = response.toTVSeason()
                        
                        // Check if show is tracked and update watched status for each episode
                        val trackedSeries = seriesTrackingDao.getSeriesById(tvId)
                        if (trackedSeries != null) {
                            val episodesWithStatus = domainSeason.episodes.map { episode ->
                                val localEpisode = seriesTrackingDao.getAllEpisodesForSeries(tvId).first().find { it.id == episode.id }
                                episode.copy(isWatched = localEpisode?.isWatched ?: false)
                            }
                            emit(NetworkResults.Success(domainSeason.copy(episodes = episodesWithStatus)))
                        } else {
                            emit(NetworkResults.Success(domainSeason))
                        }
                        return@flow
                    }
                }
            }

            // Offline or network failed - check local tracking data
            val trackedSeries = seriesTrackingDao.getSeriesById(tvId)
            if (trackedSeries != null) {
                val seasons = seriesTrackingDao.getSeasonsForSeries(tvId).first()
                val targetSeason = seasons.find { it.seasonNumber == seasonNumber }
                if (targetSeason != null) {
                    val episodes = seriesTrackingDao.getEpisodesForSeason(targetSeason.id).first()
                    emit(NetworkResults.Success(com.shalenmathew.movieflix.domain.model.TVSeason(
                        id = targetSeason.id,
                        airDate = null,
                        name = targetSeason.name,
                        overview = null,
                        posterPath = targetSeason.posterPath,
                        seasonNumber = targetSeason.seasonNumber,
                        episodes = episodes.map { it.toTVEpisode() }
                    )))
                } else {
                    emit(NetworkResults.Error(appContext.getString(R.string.msg_season_not_found_locally)))
                }
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            when (e) {
                is IOException -> emit(NetworkResults.Error(appContext.getString(R.string.msg_check_internet)))
                else -> emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
            }
        }
    }

    override fun getMovieImages(movieId: Int, includeLanguages: String?): Flow<NetworkResults<com.shalenmathew.movieflix.data.model.TVImagesResponse>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                // Combine mandatory languages (en, null) with the movie's original language
                val languages = mutableListOf("en", "null")
                includeLanguages?.let { if (it !in languages) languages.add(it) }
                
                val response = remoteDataSource.getMovieImages(movieId, languages.joinToString(","))
                if (response.isSuccessful && response.body() != null) {
                    emit(NetworkResults.Success(response.body()))
                } else {
                    emit(NetworkResults.Error(appContext.getString(R.string.msg_failed_fetch_images)))
                }
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
        }
    }

    override fun getTVImages(tvId: Int, includeLanguages: String?): Flow<NetworkResults<com.shalenmathew.movieflix.data.model.TVImagesResponse>> = flow {
        emit(NetworkResults.Loading())
        try {
            if (networkChecker(appContext)) {
                // Combine mandatory languages (en, null) with the series' original language
                val languages = mutableListOf("en", "null")
                includeLanguages?.let { if (it !in languages) languages.add(it) }
                
                val response = remoteDataSource.getTVImages(tvId, languages.joinToString(","))
                if (response.isSuccessful && response.body() != null) {
                    emit(NetworkResults.Success(response.body()))
                } else {
                    emit(NetworkResults.Error(appContext.getString(R.string.msg_failed_fetch_images)))
                }
            } else {
                emit(NetworkResults.Error(appContext.getString(R.string.no_internet_connection)))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: appContext.getString(R.string.msg_something_went_wrong)))
        }
    }

}
