package com.shalenmathew.movieflix.domain.model

data class TrackedSeries(
    val id: Int,
    val name: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val lastWatchedEpisodeId: Int? = null,
    val lastWatchedSeasonNumber: Int? = null,
    val lastWatchedEpisodeNumber: Int? = null,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)

data class TrackedSeason(
    val id: Int,
    val seriesId: Int,
    val seasonNumber: Int,
    val name: String?,
    val episodeCount: Int?,
    val posterPath: String?,
    val watchedCount: Int = 0
)

data class TrackedEpisode(
    val id: Int,
    val seriesId: Int,
    val seasonId: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String?,
    val overview: String?,
    val stillPath: String?,
    val runtime: Int?,
    val isWatched: Boolean = false
)
