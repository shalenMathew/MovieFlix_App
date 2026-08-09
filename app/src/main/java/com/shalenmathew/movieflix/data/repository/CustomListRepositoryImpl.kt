package com.shalenmathew.movieflix.data.repository

import com.shalenmathew.movieflix.data.local_storage.CustomListDao
import com.shalenmathew.movieflix.data.local_storage.entity.CustomListEntity
import com.shalenmathew.movieflix.data.local_storage.entity.CustomListMovieEntity
import com.shalenmathew.movieflix.domain.model.CustomListMovie
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.shalenmathew.movieflix.domain.model.UserCustomList
import com.shalenmathew.movieflix.domain.repository.CustomListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class CustomListRepositoryImpl @Inject constructor(
    private val customListDao: CustomListDao
) : CustomListRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllLists(): Flow<List<UserCustomList>> {
        return customListDao.getAllLists().flatMapLatest { entities ->
            val flows = entities.map { entity ->
                combine(
                    customListDao.getMovieCountInList(entity.id),
                    customListDao.getTopPostersForList(entity.id)
                ) { count, posters ->
                    UserCustomList(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        createdAt = entity.createdAt,
                        movieCount = count,
                        topPosters = posters.filterNotNull()
                    )
                }
            }
            if (flows.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                combine(flows) { it.toList() }
            }
        }
    }

    override suspend fun createList(name: String, description: String?): Long {
        return customListDao.insertList(CustomListEntity(name = name, description = description))
    }

    override suspend fun deleteList(listId: Int) {
        customListDao.deleteList(CustomListEntity(id = listId, name = "", description = null))
    }

    override suspend fun addMovieToList(listId: Int, movie: MovieResult) {
        customListDao.addMovieToList(
            CustomListMovieEntity(
                listId = listId,
                mediaId = movie.id ?: -1,
                mediaType = movie.mediaType,
                name = movie.name ?: movie.title,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                overview = movie.overview,
                voteAverage = movie.voteAverage,
                releaseDate = movie.releaseDate,
                originalLanguage = movie.originalLanguage
            )
        )
    }

    override suspend fun removeMovieFromList(listId: Int, mediaId: Int) {
        customListDao.removeMovieFromList(listId, mediaId)
    }

    override fun getMoviesInList(listId: Int): Flow<List<CustomListMovie>> {
        return customListDao.getMoviesInList(listId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun isMovieInList(listId: Int, mediaId: Int): Boolean {
        return customListDao.isMovieInList(listId, mediaId)
    }

    private fun CustomListMovieEntity.toDomain() = CustomListMovie(
        listId = listId,
        mediaId = mediaId,
        mediaType = mediaType,
        name = name,
        posterPath = posterPath,
        backdropPath = backdropPath,
        overview = overview,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        originalLanguage = originalLanguage,
        addedAt = addedAt
    )
}
