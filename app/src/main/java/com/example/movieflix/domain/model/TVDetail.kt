package com.example.movieflix.domain.model

data class TVDetail(
    val id: Int?,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double?,
    val firstAirDate: String?,
    val numberOfSeasons: Int?,
    val numberOfEpisodes: Int?,
    val seasons: List<TVSeasonBasic>
)

data class TVSeasonBasic(
    val id: Int?,
    val airDate: String?,
    val episodeCount: Int?,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val seasonNumber: Int?
)
