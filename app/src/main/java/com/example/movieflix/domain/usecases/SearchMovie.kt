package com.example.movieflix.domain.usecases

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.repository.SearchMovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchMovie (private val searchMovieRepository: SearchMovieRepository) {
    fun searchMovie(query:String):Flow<NetworkResults<MovieList>>{
        return  searchMovieRepository.searchMovie(query)
    }

    fun trendingMovies():Flow<NetworkResults<MovieList>>{
        return searchMovieRepository.trendingMovies()
    }

}