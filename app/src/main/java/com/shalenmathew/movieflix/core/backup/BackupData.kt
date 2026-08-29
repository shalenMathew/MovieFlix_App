package com.shalenmathew.movieflix.core.backup

import com.shalenmathew.movieflix.data.local_storage.entity.*

data class BackupData(
    val version: Int = 1,
    val backupTime: Long = System.currentTimeMillis(),
    val favorites: List<FavouritesEntity> = emptyList(),
    val watchList: List<WatchListEntity> = emptyList(),
    val scheduled: List<ScheduledEntity> = emptyList(),
    val series: List<SeriesTrackingEntity> = emptyList(),
    val seasons: List<SeasonTrackingEntity> = emptyList(),
    val episodes: List<EpisodeTrackingEntity> = emptyList(),
    val progress: List<SeriesProgressEntity> = emptyList(),
    val customLists: List<CustomListEntity> = emptyList(),
    val customListMovies: List<CustomListMovieEntity> = emptyList(),
    val galleryImages: List<PersonalGalleryEntity> = emptyList()
)