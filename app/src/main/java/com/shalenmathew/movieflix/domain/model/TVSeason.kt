package com.shalenmathew.movieflix.domain.model

data class TVSeason(
    val id: Int?,
    val airDate: String?,
    val episodes: List<TVEpisode>,
    val name: String?,
    val overview: String?,
    val posterPath: String?,
    val seasonNumber: Int?
)
