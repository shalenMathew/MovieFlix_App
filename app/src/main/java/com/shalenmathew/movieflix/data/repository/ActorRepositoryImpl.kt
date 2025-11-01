package com.shalenmathew.movieflix.data.repository

import android.app.Application
import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.core.utils.isNetworkAvailable
import com.shalenmathew.movieflix.data.remote.RemoteDataSource
import com.shalenmathew.movieflix.domain.model.ActorDetail
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.repository.ActorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActorRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val appContext: Application
) : ActorRepository {

    override fun getActorDetail(personId: Int): Flow<NetworkResults<ActorDetail>> = flow {
        emit(NetworkResults.Loading())

        try {
            if (isNetworkAvailable(appContext)) {
                val actorDetailResponse = withContext(Dispatchers.IO) {
                    remoteDataSource.getActorDetail(personId)
                }
                
                // Fetch external IDs with error handling
                val externalIds = try {
                    withContext(Dispatchers.IO) {
                        val response = remoteDataSource.getPersonExternalIds(personId)
                        if (response.isSuccessful) response.body() else null
                    }
                } catch (e: Exception) {
                    null // Continue even if external IDs fetch fails
                }

                // Fetch actor images with error handling
                val actorImages = try {
                    withContext(Dispatchers.IO) {
                        val response = remoteDataSource.getActorImages(personId)
                        if (response.isSuccessful) response.body() else null
                    }
                } catch (e: Exception) {
                    null // Continue even if images fetch fails
                }

                if (actorDetailResponse.isSuccessful && actorDetailResponse.body() != null) {
                    val actorData = actorDetailResponse.body()!!
                    
                    // Get a different image for backdrop (second image if available)
                    val backdropImage = actorImages?.profiles?.let { profiles ->
                        if (profiles.size > 1) profiles[1].filePath else profiles.firstOrNull()?.filePath
                    }

                    val actorDetail = ActorDetail(
                        id = actorData.id ?: 0,
                        name = actorData.name ?: "Unknown",
                        biography = actorData.biography?.takeIf { it.isNotBlank() },
                        birthday = actorData.birthday?.takeIf { it.isNotBlank() },
                        placeOfBirth = actorData.placeOfBirth?.takeIf { it.isNotBlank() },
                        profilePath = actorData.profilePath?.takeIf { it.isNotBlank() },
                        backdropImagePath = backdropImage?.takeIf { it.isNotBlank() },
                        knownForDepartment = actorData.knownForDepartment?.takeIf { it.isNotBlank() },
                        instagramId = externalIds?.instagramId?.takeIf { it.isNotBlank() },
                        twitterId = externalIds?.twitterId?.takeIf { it.isNotBlank() },
                        facebookId = externalIds?.facebookId?.takeIf { it.isNotBlank() }
                    )

                    emit(NetworkResults.Success(actorDetail))
                } else {
                    emit(NetworkResults.Error("Failed to load actor details"))
                }
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: "Unknown error occurred"))
        }
    }

    override fun getActorMoviesAndShows(personId: Int): Flow<NetworkResults<List<MovieResult>>> = flow {
        emit(NetworkResults.Loading())

        try {
            if (isNetworkAvailable(appContext)) {
                // Fetch both movie and TV credits with error handling
                val movies = try {
                    withContext(Dispatchers.IO) {
                        val response = remoteDataSource.getActorMovieCredits(personId)
                        if (response.isSuccessful) response.body()?.cast else null
                    }
                } catch (e: Exception) {
                    null
                }

                val tvShows = try {
                    withContext(Dispatchers.IO) {
                        val response = remoteDataSource.getActorTVCredits(personId)
                        if (response.isSuccessful) response.body()?.cast else null
                    }
                } catch (e: Exception) {
                    null
                }

                val allContent = mutableListOf<MovieResult>()

                // Add movies
                movies?.forEach { movie ->
                    allContent.add(
                        MovieResult(
                            backdropPath = movie.backdropPath,
                            genreIds = movie.genreIds,
                            id = movie.id,
                            originalLanguage = movie.originalLanguage,
                            originalTitle = movie.originalTitle,
                            name = movie.title,
                            overview = movie.overview,
                            posterPath = movie.posterPath,
                            releaseDate = movie.releaseDate,
                            title = movie.title,
                            voteAverage = movie.voteAverage,
                            mediaType = "movie"
                        )
                    )
                }

                // Add TV shows
                tvShows?.forEach { show ->
                    allContent.add(
                        MovieResult(
                            backdropPath = show.backdropPath,
                            genreIds = show.genreIds,
                            id = show.id,
                            originalLanguage = show.originalLanguage,
                            originalTitle = show.originalName,
                            name = show.name,
                            overview = show.overview,
                            posterPath = show.posterPath,
                            releaseDate = show.firstAirDate,
                            title = show.name,
                            voteAverage = show.voteAverage,
                            mediaType = "tv"
                        )
                    )
                }

                // Sort by vote average
                val sortedContent = allContent.sortedByDescending { it.voteAverage }

                emit(NetworkResults.Success(sortedContent))
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
