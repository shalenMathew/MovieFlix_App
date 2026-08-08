package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series_progress_table")
data class SeriesProgressEntity(
    @PrimaryKey
    val seriesId: Int,
    val lastWatchedSeasonNumber: Int?,
    val lastWatchedEpisodeNumber: Int?,
    val lastWatchedEpisodeId: Int?,
    val lastUpdated: Long = System.currentTimeMillis()
)
