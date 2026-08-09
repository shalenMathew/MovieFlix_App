package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "custom_list_movie_table",
    primaryKeys = ["listId", "mediaId"],
    foreignKeys = [
        ForeignKey(
            entity = CustomListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class CustomListMovieEntity(
    val listId: Int,
    val mediaId: Int,
    val mediaType: String?,
    val name: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val voteAverage: Double?,
    val releaseDate: String?,
    val originalLanguage: String?,
    val addedAt: Long = System.currentTimeMillis()
)
