package com.example.movieflix.domain.model

data class TVEpisode(
    val id: Int?,
    val airDate: String?,
    val episodeNumber: Int?,
    val name: String?,
    val overview: String?,
    val runtime: Int?,
    val seasonNumber: Int?,
    val stillPath: String?,
    val voteAverage: Double?
)
