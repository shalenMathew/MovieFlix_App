package com.example.movieflix.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MovieResult(
    val backdropPath: String?=null,
    val genreIds: List<Int>?=null,
    val id: Int?=null,
    val originalLanguage: String?=null,
    val originalTitle: String?=null,
    val name: String?=null,
    val overview: String?=null,
    val posterPath: String?=null,
    val releaseDate: String?=null,
    val title: String?=null,
    val voteAverage: Double?=null,
    val mediaType: String?=null
): Parcelable  // parcelable is used to send data across activities