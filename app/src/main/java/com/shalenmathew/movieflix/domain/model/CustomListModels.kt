package com.shalenmathew.movieflix.domain.model

data class UserCustomList(
    val id: Int,
    val name: String,
    val description: String?,
    val createdAt: Long,
    val movieCount: Int = 0,
    val topPosters: List<String> = emptyList()
)

data class CustomListMovie(
    val listId: Int,
    val mediaId: Int,
    val mediaType: String?,
    val name: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val voteAverage: Double?,
    val releaseDate: String?,
    val originalLanguage: String?,
    val addedAt: Long
)
