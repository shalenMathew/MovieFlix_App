package com.shalenmathew.movieflix.domain.repository

import com.shalenmathew.movieflix.domain.model.CustomListMovie
import com.shalenmathew.movieflix.domain.model.UserCustomList
import kotlinx.coroutines.flow.Flow

interface CustomListRepository {
    fun getAllLists(): Flow<List<UserCustomList>>
    suspend fun createList(name: String, description: String?): Long
    suspend fun deleteList(listId: Int)
    suspend fun addMovieToList(listId: Int, movie: com.shalenmathew.movieflix.domain.model.MovieResult)
    suspend fun removeMovieFromList(listId: Int, mediaId: Int)
    suspend fun updateMoviePosterAcrossLists(mediaId: Int, posterPath: String)
    suspend fun updateMovieBannerAcrossLists(mediaId: Int, bannerPath: String)
    fun getMoviesInList(listId: Int): Flow<List<CustomListMovie>>
    suspend fun isMovieInList(listId: Int, mediaId: Int): Boolean
}
