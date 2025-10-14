package com.example.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.domain.model.MovieResult

@Entity(Constants.FAVOURITES_TABLE_NAME)
data class FavouritesEntity(
    @PrimaryKey val id:Int,
    val movieResult: MovieResult
)