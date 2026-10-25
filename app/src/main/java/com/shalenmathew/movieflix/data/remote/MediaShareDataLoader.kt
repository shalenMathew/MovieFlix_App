package com.shalenmathew.movieflix.data.remote

import com.shalenmathew.movieflix.data.network.ApiClient
import com.shalenmathew.movieflix.domain.model.MediaShareData
import com.shalenmathew.movieflix.domain.model.MovieResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import retrofit2.Response
import javax.inject.Inject

class MediaShareDataLoader @Inject constructor(
    private val apiClient: ApiClient
) {
    suspend fun load(movie: MovieResult): MediaShareData = coroutineScope {
        val movieId = movie.id ?: -1
        if (movieId == -1) return@coroutineScope MediaShareData()

        val isTv = movie.mediaType.equals("tv", ignoreCase = true)
        val tvDetailsDeferred = if (isTv) {
            async { fetchOrNull { apiClient.fetchTVDetailApiCall(movieId) } }
        } else {
            null
        }

        val providerDeferred = async {
            val response = fetchOrNull {
                if (isTv) apiClient.getTVWatchProvidersApiCall(movieId)
                else apiClient.getMovieWatchProvidersApiCall(movieId)
            }
            if (response?.isSuccessful == true) {
                val results = response.body()?.results
                (results?.IN?.flatrate?.firstOrNull()
                    ?: results?.IN?.buy?.firstOrNull()
                    ?: results?.IN?.rent?.firstOrNull())?.providerName
            } else {
                null
            }
        }

        val creditsDeferred = async {
            val response = fetchOrNull {
                if (isTv) apiClient.fetchTVCastApiCall(movieId)
                else apiClient.fetchMovieCastApiCall(movieId)
            }
            val castBody = response?.takeIf { it.isSuccessful }?.body()
            val crewList = castBody?.crew.orEmpty()
            val director = crewList.firstOrNull { it.job.equals("Director", ignoreCase = true) }
                ?: crewList.firstOrNull { it.department.equals("Directing", ignoreCase = true) }
                ?: crewList.firstOrNull { it.job.equals("Executive Producer", ignoreCase = true) }
            val leadCast = castBody?.cast?.firstOrNull()
            Triple(director?.name, leadCast?.name, leadCast?.character)
        }

        val tvDetails = tvDetailsDeferred?.await()?.takeIf { it.isSuccessful }?.body()
        val seasons = tvDetails?.seasons
            ?.filter { (it.season_number ?: 0) > 0 }
            .orEmpty()
        val numberOfSeasons = seasons.size
        val maxEpisodes = seasons.maxOfOrNull { it.episode_count ?: 0 } ?: 0
        val isHeatmapAllowed = !isTv || (numberOfSeasons <= MAX_SEASONS && maxEpisodes <= MAX_EPISODES)

        val seasonRatings = if (isTv && numberOfSeasons <= MAX_SEASONS && maxEpisodes <= MAX_EPISODES) {
            seasons.take(MAX_SEASONS).map { season ->
                async {
                    val seasonNumber = season.season_number ?: 1
                    val response = fetchOrNull {
                        apiClient.fetchTVSeasonApiCall(movieId, seasonNumber)
                    }
                    seasonNumber to if (response?.isSuccessful == true) {
                        response.body()?.episodes.orEmpty().map { it.vote_average }
                    } else {
                        emptyList()
                    }
                }
            }.awaitAll().toMap()
        } else {
            emptyMap()
        }

        val (directorName, castName, characterName) = creditsDeferred.await()
        MediaShareData(
            clientProvider = providerDeferred.await(),
            directorName = directorName,
            castName = castName,
            characterName = characterName,
            isHeatmapAllowed = isHeatmapAllowed,
            seasonRatingsMap = seasonRatings
        )
    }

    private suspend fun <T> fetchOrNull(fetch: suspend () -> Response<T>): Response<T>? {
        return try {
            fetch()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            exception.printStackTrace()
            null
        }
    }

    private companion object {
        const val MAX_SEASONS = 10
        const val MAX_EPISODES = 30
    }
}
