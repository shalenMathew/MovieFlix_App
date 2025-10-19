package com.example.movieflix.data.remote

import com.example.movieflix.data.model.CastResponse
import com.example.movieflix.data.model.MovieResponseList
import com.example.movieflix.data.model.MovieResponseVideoResultList
import com.example.movieflix.data.model.PersonExternalIdsResponse
import com.example.movieflix.data.model.WhereToWatchProviderResponse
import com.example.movieflix.data.network.ApiClient
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    suspend fun getUpcomingMovies(page: Int = 1):Response<MovieResponseList>{
      return  apiClient.getUpcomingMoviesApiCall(page = page)
    }

    suspend fun getPopularMovies(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getPopularMoviesApiCall(page = page)
    }

    suspend fun getTrendingMovies(page: Int = 1):Response<MovieResponseList>{
        return apiClient.getTrendingApiCall(page = page)
    }

    suspend fun getTopRatedTV(page: Int = 1):Response<MovieResponseList>{
        return apiClient.getTopRatedTVApiCall(page = page)
    }

    suspend fun getNetflixShows(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getNetflixShowsApiCall(page = page)
    }

    suspend fun getNowPlayingMovies(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getNowPlayingMoviesApiCall(page = page)
    }

    suspend fun getAmazonPrimeShows(page: Int = 1):Response<MovieResponseList>{
        return   apiClient.getAmazonPrimeShowsApi(page = page)
    }

    suspend fun getBollywoodMovies(page: Int = 1):Response<MovieResponseList>{
        return apiClient.getBollywoodMoviesApiCall(page = page)
    }

    suspend fun getMovieTrailer(movieId:Int):Response<MovieResponseVideoResultList>{
        return apiClient.fetchMovieTrailerApiCall(movieId)
    }

    suspend fun getTVTrailer(tvId:Int):Response<MovieResponseVideoResultList>{
        return apiClient.fetchTVTrailerApiCall(tvId)
    }

    suspend fun getRecommendation(movieId:Int):Response<MovieResponseList>{
        return apiClient.fetchRecommendationApiCall(movieId)
    }

    suspend fun getWhereToWatchProviders(movieId:Int):Response<WhereToWatchProviderResponse>{
        return apiClient.getMovieWatchProvidersApiCall(movieId)
    }

    suspend fun getSearchResult(query:String):Response<MovieResponseList>{
        return apiClient.fetchMovieSearchedResultsApiCall(searchQuery = query)
    }

    suspend fun getMovieCast(movieId: Int): Response<CastResponse> {
        return apiClient.fetchMovieCastApiCall(movieId)
    }

    suspend fun getTVCast(tvId: Int): Response<CastResponse> {
        return apiClient.fetchTVCastApiCall(tvId)
    }

    suspend fun getPersonExternalIds(personId: Int): Response<PersonExternalIdsResponse> {
        return apiClient.fetchPersonExternalIdsApiCall(personId)
    }

}