package com.example.movieflix.domain.usecases

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.HomeFeed
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.model.MovieVideoResultList
import com.example.movieflix.domain.model.WatchProviders
import com.example.movieflix.domain.repository.MovieInfoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class  GetMovieInfo @Inject constructor(private val movieInfoRepository: MovieInfoRepository) {

    fun getMovieInfo(): Flow<NetworkResults<HomeFeedData>> {
      return  movieInfoRepository.getHomeFeedData()
    }

    fun getMovieTrailer(movieId:Int):Flow<NetworkResults<MovieVideoResultList>>{
        return movieInfoRepository.getMovieTrailer(movieId)
    }

    fun getRecommendation(movieId: Int):Flow<NetworkResults<MovieList>>{
        return  movieInfoRepository.getRecommendation(movieId)
    }

    fun getWhereToWatchProviders(movieId:Int):Flow<NetworkResults<WatchProviders>>{
        return movieInfoRepository.getWhereToWatchProvider(movieId)
    }
}