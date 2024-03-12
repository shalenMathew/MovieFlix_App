package com.example.movieflix.domain.usecases

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RandomRecommendation @Inject constructor(private val recommendationRepository: RecommendationRepository) {
    fun searchMovie(query:String):Flow<NetworkResults<MovieList>>{
        return  recommendationRepository.searchMovie(query)
    }
}