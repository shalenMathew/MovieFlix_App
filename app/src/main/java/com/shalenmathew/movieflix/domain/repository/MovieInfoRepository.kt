package com.shalenmathew.movieflix.domain.repository

import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.CastMember
import com.shalenmathew.movieflix.domain.model.CrewMember
import com.shalenmathew.movieflix.domain.model.HomeFeedData
import com.shalenmathew.movieflix.domain.model.MovieList
import com.shalenmathew.movieflix.domain.model.MediaVideoResultList
import com.shalenmathew.movieflix.domain.model.TVDetail
import com.shalenmathew.movieflix.domain.model.TVSeason
import com.shalenmathew.movieflix.domain.model.WatchProviders
import kotlinx.coroutines.flow.Flow

interface MovieInfoRepository {

// creating an abstraction

fun getHomeFeedData():Flow<NetworkResults<HomeFeedData>>
// here a curious question arises why can't we use HomeFeedDataResponse from Data layer instead of HomeDeedData in Flow<NetworkResults<HomeFeedData>>
// i mean both do the same thing right, remember the reason we declared same model class in different layer coz that the each layer dont have to
// be dependent on model class from other layers so every class should use model class from their own layers

fun getMovieTrailer(movieId:Int):Flow<NetworkResults<MediaVideoResultList>>

fun getTVTrailer(tvId:Int):Flow<NetworkResults<MediaVideoResultList>>

fun getRecommendation(movieId:Int):Flow<NetworkResults<MovieList>>

fun getWhereToWatchProvider(movieId:Int):Flow<NetworkResults<WatchProviders>>

fun getTVWhereToWatchProvider(tvId:Int):Flow<NetworkResults<WatchProviders>>

// Pagination support for home feed categories
fun loadMoreMoviesForCategory(categoryTitle: String, page: Int): Flow<NetworkResults<MovieList>>

fun getMovieCast(movieId: Int): Flow<NetworkResults<List<CastMember>>>

fun getTVCast(tvId: Int): Flow<NetworkResults<List<CastMember>>>

fun getMovieCrew(movieId: Int): Flow<NetworkResults<List<CrewMember>>>

fun getTVCrew(tvId: Int): Flow<NetworkResults<List<CrewMember>>>

fun getTVDetail(tvId: Int): Flow<NetworkResults<TVDetail>>

fun getTVSeason(tvId: Int, seasonNumber: Int): Flow<NetworkResults<TVSeason>>

}