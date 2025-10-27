package com.example.movieflix.domain.usecases

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.CastMember
import com.example.movieflix.domain.model.CrewMember
import com.example.movieflix.domain.model.HomeFeedData
import com.example.movieflix.domain.model.MovieList
import com.example.movieflix.domain.model.MediaVideoResultList
import com.example.movieflix.domain.model.TVDetail
import com.example.movieflix.domain.model.TVSeason
import com.example.movieflix.domain.model.WatchProviders
import com.example.movieflix.domain.repository.MovieInfoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class  GetMovieInfo @Inject constructor(private val movieInfoRepository: MovieInfoRepository) {

    fun getMovieInfo(): Flow<NetworkResults<HomeFeedData>> {
      return  movieInfoRepository.getHomeFeedData()
    }

    fun getMovieTrailer(movieId:Int):Flow<NetworkResults<MediaVideoResultList>>{
        return movieInfoRepository.getMovieTrailer(movieId)
    }

    fun getTVTrailer(tvId:Int):Flow<NetworkResults<MediaVideoResultList>>{
        return movieInfoRepository.getTVTrailer(tvId)
    }

    fun getRecommendation(movieId: Int):Flow<NetworkResults<MovieList>>{
        return  movieInfoRepository.getRecommendation(movieId)
    }

    fun getWhereToWatchProviders(movieId:Int):Flow<NetworkResults<WatchProviders>>{
        return movieInfoRepository.getWhereToWatchProvider(movieId)
    }

    fun getTVWhereToWatchProviders(tvId:Int):Flow<NetworkResults<WatchProviders>>{
        return movieInfoRepository.getTVWhereToWatchProvider(tvId)
    }

    fun loadMoreMoviesForCategory(categoryTitle: String, page: Int): Flow<NetworkResults<MovieList>> {
        return movieInfoRepository.loadMoreMoviesForCategory(categoryTitle, page)
    }

    fun getMovieCast(movieId: Int): Flow<NetworkResults<List<CastMember>>> {
        return movieInfoRepository.getMovieCast(movieId)
    }

    fun getTVCast(tvId: Int): Flow<NetworkResults<List<CastMember>>> {
        return movieInfoRepository.getTVCast(tvId)
    }

    fun getMovieCrew(movieId: Int): Flow<NetworkResults<List<CrewMember>>> {
        return movieInfoRepository.getMovieCrew(movieId)
    }

    fun getTVCrew(tvId: Int): Flow<NetworkResults<List<CrewMember>>> {
        return movieInfoRepository.getTVCrew(tvId)
    }

    fun getTVDetail(tvId: Int): Flow<NetworkResults<TVDetail>> {
        return movieInfoRepository.getTVDetail(tvId)
    }

    fun getTVSeason(tvId: Int, seasonNumber: Int): Flow<NetworkResults<TVSeason>> {
        return movieInfoRepository.getTVSeason(tvId, seasonNumber)
    }
}