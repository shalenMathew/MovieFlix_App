package com.shalenmathew.movieflix.domain.usecases

import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.MovieList
import com.shalenmathew.movieflix.domain.repository.SearchMovieRepository
import kotlinx.coroutines.flow.Flow

class SearchMovie (private val searchMovieRepository: SearchMovieRepository) {
    fun searchMovie(query:String):Flow<NetworkResults<MovieList>>{
        return  searchMovieRepository.searchMovie(query)
    }

    fun trendingMovies():Flow<NetworkResults<MovieList>>{
        return searchMovieRepository.trendingMovies()
    }

}