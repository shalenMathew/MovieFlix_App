package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shalenmathew.movieflix.core.utils.Constants

@Entity(
    tableName = Constants.SEASONS_TRACKING_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = SeriesTrackingEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId"])]
)
data class SeasonTrackingEntity(
    @PrimaryKey val id: Int,
    val seriesId: Int,
    val seasonNumber: Int,
    val name: String?,
    val episodeCount: Int?,
    val posterPath: String?
)
