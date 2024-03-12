package com.example.movieflix.domain.repository

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.MovieList
import kotlinx.coroutines.flow.Flow

interface SearchMovieRepository {
    fun searchMovie(query:String): Flow<NetworkResults<MovieList>>

    fun trendingMovies():Flow<NetworkResults<MovieList>>
}