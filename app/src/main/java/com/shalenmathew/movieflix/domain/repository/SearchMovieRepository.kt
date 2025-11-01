package com.shalenmathew.movieflix.domain.repository

import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.MovieList
import kotlinx.coroutines.flow.Flow

interface SearchMovieRepository {
    fun searchMovie(query:String): Flow<NetworkResults<MovieList>>

    fun trendingMovies():Flow<NetworkResults<MovieList>>
}