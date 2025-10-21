package com.example.movieflix.data.repository

import android.app.Application
import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.core.utils.isNetworkAvailable
import com.example.movieflix.data.remote.RemoteDataSource
import com.example.movieflix.domain.model.ActorDetail
import com.example.movieflix.domain.model.MovieResult
import com.example.movieflix.domain.repository.ActorRepository
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
                
                val externalIdsResponse = withContext(Dispatchers.IO) {
                    remoteDataSource.getPersonExternalIds(personId)
                }

                val actorImagesResponse = withContext(Dispatchers.IO) {
                    remoteDataSource.getActorImages(personId)
                }

                if (actorDetailResponse.isSuccessful && actorDetailResponse.body() != null) {
                    val actorData = actorDetailResponse.body()!!
                    val externalIds = externalIdsResponse.body()
                    
                    // Get a different image for backdrop (second image if available)
                    val backdropImage = actorImagesResponse.body()?.profiles?.let { profiles ->
                        if (profiles.size > 1) profiles[1].filePath else profiles.firstOrNull()?.filePath
                    }

                    val actorDetail = ActorDetail(
                        id = actorData.id ?: 0,
                        name = actorData.name ?: "",
                        biography = actorData.biography,
                        birthday = actorData.birthday,
                        placeOfBirth = actorData.placeOfBirth,
                        profilePath = actorData.profilePath,
                        backdropImagePath = backdropImage,
                        knownForDepartment = actorData.knownForDepartment,
                        instagramId = externalIds?.instagramId,
                        twitterId = externalIds?.twitterId,
                        facebookId = externalIds?.facebookId
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

    override fun getActorMovies(personId: Int): Flow<NetworkResults<List<MovieResult>>> = flow {
        emit(NetworkResults.Loading())

        try {
            if (isNetworkAvailable(appContext)) {
                val moviesResponse = withContext(Dispatchers.IO) {
                    remoteDataSource.getActorMovieCredits(personId)
                }

                if (moviesResponse.isSuccessful && moviesResponse.body() != null) {
                    val movies = moviesResponse.body()!!.cast?.map { movie ->
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
                    }?.sortedByDescending { it.voteAverage } ?: emptyList()

                    emit(NetworkResults.Success(movies))
                } else {
                    emit(NetworkResults.Error("Failed to load actor movies"))
                }
            } else {
                emit(NetworkResults.Error("No internet connection"))
            }
        } catch (e: Exception) {
            emit(NetworkResults.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
