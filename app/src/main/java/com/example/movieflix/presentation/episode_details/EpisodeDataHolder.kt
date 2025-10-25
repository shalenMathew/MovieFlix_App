package com.example.movieflix.presentation.episode_details

import com.example.movieflix.domain.model.TVEpisode

/**
 * Temporary data holder to avoid passing large episode lists through Intent
 * This prevents TransactionTooLargeException and improves performance
 */
object EpisodeDataHolder {
    var episodes: List<TVEpisode> = emptyList()
        private set
    
    var seasonNumber: Int = 1
        private set
    
    var tvShowName: String? = null
        private set
    
    var totalSeasons: Int = 1
        private set
    
    var tvShowId: Int? = null
        private set
    
    var onSeasonChangeCallback: ((Int) -> Unit)? = null
        private set
    
    fun setData(
        episodeList: List<TVEpisode>,
        season: Int,
        showName: String?,
        totalSeasons: Int = 1,
        tvShowId: Int? = null,
        onSeasonChange: ((Int) -> Unit)? = null
    ) {
        episodes = episodeList
        seasonNumber = season
        tvShowName = showName
        this.totalSeasons = totalSeasons
        this.tvShowId = tvShowId
        onSeasonChangeCallback = onSeasonChange
    }
    
    fun clear() {
        episodes = emptyList()
        seasonNumber = 1
        tvShowName = null
        totalSeasons = 1
        tvShowId = null
        onSeasonChangeCallback = null
    }
}
