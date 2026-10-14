package com.shalenmathew.movieflix.domain.model

data class MediaShareData(
    val clientProvider: String? = null,
    val directorName: String? = null,
    val castName: String? = null,
    val characterName: String? = null,
    val isHeatmapAllowed: Boolean = true,
    val seasonRatingsMap: Map<Int, List<Double?>> = emptyMap()
)
