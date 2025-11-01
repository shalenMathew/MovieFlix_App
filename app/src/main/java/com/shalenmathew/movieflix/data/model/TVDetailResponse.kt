package com.shalenmathew.movieflix.data.model

import com.shalenmathew.movieflix.domain.model.TVDetail

data class TVDetailResponse(
    val id: Int?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val first_air_date: String?,
    val number_of_seasons: Int?,
    val number_of_episodes: Int?,
    val seasons: List<TVSeasonBasicResponse>?
) {
    fun toTVDetail(): TVDetail {
        return TVDetail(
            id = id,
            name = name,
            overview = overview,
            posterPath = poster_path,
            backdropPath = backdrop_path,
            voteAverage = vote_average,
            firstAirDate = first_air_date,
            numberOfSeasons = number_of_seasons,
            numberOfEpisodes = number_of_episodes,
            seasons = seasons?.map { it.toTVSeasonBasic() } ?: emptyList()
        )
    }
}

data class TVSeasonBasicResponse(
    val air_date: String?,
    val episode_count: Int?,
    val id: Int?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int?
) {
    fun toTVSeasonBasic(): com.shalenmathew.movieflix.domain.model.TVSeasonBasic {
        return com.shalenmathew.movieflix.domain.model.TVSeasonBasic(
            id = id,
            airDate = air_date,
            episodeCount = episode_count,
            name = name,
            overview = overview,
            posterPath = poster_path,
            seasonNumber = season_number
        )
    }
}
