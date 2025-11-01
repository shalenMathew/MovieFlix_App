package com.shalenmathew.movieflix.data.model

import com.shalenmathew.movieflix.domain.model.TVEpisode

data class TVEpisodeResponse(
    val air_date: String?,
    val episode_number: Int?,
    val id: Int?,
    val name: String?,
    val overview: String?,
    val production_code: String?,
    val runtime: Int?,
    val season_number: Int?,
    val still_path: String?,
    val vote_average: Double?,
    val vote_count: Int?
) {
    fun toTVEpisode(): TVEpisode {
        return TVEpisode(
            id = id,
            airDate = air_date,
            episodeNumber = episode_number,
            name = name,
            overview = overview,
            runtime = runtime,
            seasonNumber = season_number,
            stillPath = still_path,
            voteAverage = vote_average
        )
    }
}
