package com.example.movieflix.data.remote

import com.example.movieflix.data.model.MovieResponseList
import com.example.movieflix.data.model.MovieResponseVideoResultList
import com.example.movieflix.data.model.WhereToWatchProviderResponse
import com.example.movieflix.data.network.ApiClient
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    suspend fun getUpcomingMovies():Response<MovieResponseList>{
      return  apiClient.getUpcomingMoviesApiCall()
    }

    suspend fun getPopularMovies():Response<MovieResponseList>{
        return   apiClient.getPopularMoviesApiCall()
    }

    suspend fun getTrendingMovies():Response<MovieResponseList>{
        return apiClient.getTrendingApiCall()
    }

    suspend fun getTopRatedTV():Response<MovieResponseList>{
        return apiClient.getTopRatedTVApiCall()
    }

    suspend fun getNetflixShows():Response<MovieResponseList>{
        return   apiClient.getNetflixShowsApiCall()
    }

    suspend fun getNowPlayingMovies():Response<MovieResponseList>{
        return   apiClient.getNowPlayingMoviesApiCall()
    }

    suspend fun getAmazonPrimeShows():Response<MovieResponseList>{
        return   apiClient.getAmazonPrimeShowsApi()
    }

    suspend fun getBollywoodMovies():Response<MovieResponseList>{
        return apiClient.getBollywoodMoviesApiCall()
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



}