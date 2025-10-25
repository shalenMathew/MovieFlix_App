package com.example.movieflix.data.network

import com.example.movieflix.data.model.ActorDetailResponse
import com.example.movieflix.data.model.ActorImagesResponse
import com.example.movieflix.data.model.ActorMovieCreditsResponse
import com.example.movieflix.data.model.ActorTVCreditsResponse
import com.example.movieflix.data.model.CastResponse
import com.example.movieflix.data.model.MovieResponseList
import com.example.movieflix.data.model.MovieResponseVideoResultList
import com.example.movieflix.data.model.PersonExternalIdsResponse
import com.example.movieflix.data.model.TVDetailResponse
import com.example.movieflix.data.model.TVSeasonResponse
import com.example.movieflix.data.model.WhereToWatchProviderResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiClient {

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
        @Path("media_type") mediaType: String = "all",
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
        @Query("with_networks") networkId: String = "213", // network code for Netflix
        @Query("page") page: Int = 1
    ):Response<MovieResponseList>

    @GET("3/movie/now_playing")
    suspend fun getNowPlayingMoviesApiCall(
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieResponseList>

    @GET("3/discover/tv")
    suspend fun getAmazonPrimeShowsApi(
        @Query("language") lang: String? = "en-US",
        @Query("with_networks") networkId: String = "1024", // network code for Amazon Prime
        @Query("page") page: Int = 1
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
    suspend fun fetchMovieTrailerApiCall(
        @Path("movie_id") movieId: Int,
        @Query("language") lang: String? = "en-US",
        @Query("page") page: Int = 1
    ): Response<MovieResponseVideoResultList>

    @GET("3/tv/{tv_id}/videos")
    suspend fun fetchTVTrailerApiCall(
        @Path("tv_id") tvId: Int,
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

    @GET("3/movie/{movie_id}/credits")
    suspend fun fetchMovieCastApiCall(
        @Path("movie_id") movieId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<CastResponse>

    @GET("3/tv/{tv_id}/credits")
    suspend fun fetchTVCastApiCall(
        @Path("tv_id") tvId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<CastResponse>

    @GET("3/person/{person_id}/external_ids")
    suspend fun fetchPersonExternalIdsApiCall(
        @Path("person_id") personId: Int
    ): Response<PersonExternalIdsResponse>

    @GET("3/person/{person_id}")
    suspend fun fetchActorDetailApiCall(
        @Path("person_id") personId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<ActorDetailResponse>

    @GET("3/person/{person_id}/movie_credits")
    suspend fun fetchActorMovieCreditsApiCall(
        @Path("person_id") personId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<ActorMovieCreditsResponse>

    @GET("3/person/{person_id}/images")
    suspend fun fetchActorImagesApiCall(
        @Path("person_id") personId: Int
    ): Response<ActorImagesResponse>

    @GET("3/person/{person_id}/tv_credits")
    suspend fun fetchActorTVCreditsApiCall(
        @Path("person_id") personId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<ActorTVCreditsResponse>

    @GET("3/tv/{tv_id}")
    suspend fun fetchTVDetailApiCall(
        @Path("tv_id") tvId: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<TVDetailResponse>

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun fetchTVSeasonApiCall(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("language") lang: String? = "en-US"
    ): Response<TVSeasonResponse>

}