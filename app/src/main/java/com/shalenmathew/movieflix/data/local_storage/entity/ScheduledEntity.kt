package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.domain.model.MovieResult

@Entity(Constants.SCHEDULED_TABLE_NAME)
data class ScheduledEntity(
    @PrimaryKey val id: Int,
    val movieResult: MovieResult,
    val scheduledDate: Long // Timestamp in milliseconds
)
