package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shalenmathew.movieflix.core.utils.Constants

@Entity(
    tableName = Constants.EPISODES_TRACKING_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = SeriesTrackingEntity::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonTrackingEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["seriesId"]),
        Index(value = ["seasonId"])
    ]
)
data class EpisodeTrackingEntity(
    @PrimaryKey val id: Int,
    val seriesId: Int,
    val seasonId: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String?,
    val overview: String?,
    val stillPath: String?,
    val runtime: Int?,
    val isWatched: Boolean = false
) {
    fun toTVEpisode(): com.shalenmathew.movieflix.domain.model.TVEpisode {
        return com.shalenmathew.movieflix.domain.model.TVEpisode(
            id = id,
            airDate = null,
            episodeNumber = episodeNumber,
            name = name,
            overview = overview,
            runtime = runtime,
            seasonNumber = seasonNumber,
            stillPath = stillPath,
            voteAverage = null,
            isWatched = isWatched
        )
    }
}
