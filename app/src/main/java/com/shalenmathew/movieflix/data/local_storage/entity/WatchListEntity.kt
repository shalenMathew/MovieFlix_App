package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.domain.model.MovieResult

@Entity(Constants.WATCHLIST_TABLE_NAME)
data class WatchListEntity(
    @PrimaryKey val id:Int,
    val movieResult: MovieResult,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val insertedAt: String?,
)