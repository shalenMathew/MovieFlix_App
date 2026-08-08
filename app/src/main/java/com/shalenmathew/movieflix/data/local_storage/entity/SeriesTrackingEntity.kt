package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shalenmathew.movieflix.core.utils.Constants

@Entity(tableName = Constants.SERIES_TRACKING_TABLE_NAME)
data class SeriesTrackingEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val lastUpdated: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING" // PENDING, COMPLETED, FAILED
)
