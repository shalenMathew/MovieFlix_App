package com.shalenmathew.movieflix.data.local_storage.entity

import com.shalenmathew.movieflix.domain.model.MovieResult

data class IdAndMovieResult(
    val id: Int,
    val movieResult: MovieResult,
)