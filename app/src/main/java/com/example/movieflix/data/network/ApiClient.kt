package com.example.movieflix.data.network

import com.example.movieflix.data.model.MovieResponseList
import com.example.movieflix.data.model.MovieResponseVideoResult
import com.example.movieflix.data.model.MovieResponseVideoResultList
import com.example.movieflix.data.model.WhereToWatchProviderResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiClient {

    // https://api.themoviedb.org/3/movie/upcoming?language=en-US&page=1&region=US&api_key=edc78b0442bb6d9f5edd90eb623fa9c0

    @GET("3/movie/upcoming")
    suspend fun getUpcomingMoviesApiCall(
        @Query("language") language:String? ="en-US",
        @Query("page") page:Int=1,
        @Query("region") region:String="US"
    ):Response<MovieResponseList>

    @GET("3/movie/popular")
    suspend fun getPopularMoviesApiCall(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ):Response<MovieResponseList>


    // this for search
    @GET("3/trending/{media_type}/{time_window}")
    suspend fun getTrendingApiCall(
        @Path("media_type") mediaType: String = "movie",
        @Path("time_window") timeWindow: String = "day",
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieResponseList>

    @GET("3/tv/top_rated")
    suspend fun getTopRatedTVApiCall(
        @Query("language") language:String? ="en-US",
        @Query("page") page:Int=1
    ):Response<MovieResponseList>

    @GET("3/discover/tv")
    suspend fun getNetflixShowsApiCall(
        @Query("language") lang: String? = "en-US",
        @Query("with_networks") page: String = "213" // network code for Netflix
    ):Response<MovieResponseList>

    @GET("3/movie/now_playing")
    suspend fun getNowPlayingMoviesApiCall(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieResponseList>

    @GET("3/discover/tv")
    suspend fun getAmazonPrimeShowsApi(
        @Query("language") lang: String? = "en-US",
        @Query("with_networks") page: String = "1024"
    ):Response<MovieResponseList>

    @GET("3/discover/movie")
    suspend fun getBollywoodMoviesApiCall(
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("primary_release_date.gte") releaseDateGreaterThan: String = "2012-08-01",
        @Query("page") page: Int = 1,
        @Query("region") region: String = "IN",
        @Query("with_release_type") releaseType: String = "3|2",
        @Query("watch_region") watchRegion: String = "IN",
        @Query("language") lang: String? = "hi-IN",
        @Query("with_original_language") origLang: String = "hi",
    ): Response<MovieResponseList>

    @GET("3/movie/{movie_id}/videos")
    suspend fun fetchMovieVideoApiCall(
        @Path("movie_id") movieId: Int,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieResponseVideoResultList>


    @GET("3/movie/{movie_id}/recommendations")
    suspend fun fetchRecommendationApiCall(
        @Path("movie_id") movieId:Int,
        @Query("language") language: String? ="en-US",
        @Query("page") page:Int=1,
    ):Response<MovieResponseList>


    @GET("3/movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProvidersApiCall(
        @Path("movie_id") movieId: Int
    ): Response<WhereToWatchProviderResponse>

    @GET("3/search/multi")
    suspend fun fetchMovieSearchedResultsApiCall(
        @Query("language") lang: String? = "en-US",
        @Query("include_adult") includeAdult: Boolean=false,
        @Query("query") searchQuery:String
    ): Response<MovieResponseList>

    @GET("3/search/movie")
    suspend fun fetchRandomMovies(
        @Query("language") lang: String? = "en-US",
        @Query("include_adult") includeAdult: Boolean=false,
        @Query("query") searchQuery:String
    ): Response<MovieResponseList>

}