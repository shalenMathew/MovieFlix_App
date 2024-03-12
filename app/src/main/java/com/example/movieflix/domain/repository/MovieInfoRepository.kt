package com.example.movieflix.domain.repository

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.model.MovieVideoResultList
import com.example.movieflix.domain.model.WatchProviders
import kotlinx.coroutines.flow.Flow

interface MovieInfoRepository {

// creating an abstraction

fun getHomeFeedData():Flow<NetworkResults<HomeFeedData>>
// here a curious question arises why can't we use HomeFeedDataResponse from Data layer instead of HomeDeedData in Flow<NetworkResults<HomeFeedData>>
// i mean both do the same thing right, remember the reason we declared same model class in different layer coz that the each layer dont have to
// be dependent on model class from other layers so every class should use model class from their own layers

fun getMovieTrailer(movieId:Int):Flow<NetworkResults<MovieVideoResultList>>

fun getRecommendation(movieId:Int):Flow<NetworkResults<MovieList>>

fun getWhereToWatchProvider(movieId:Int):Flow<NetworkResults<WatchProviders>>



}