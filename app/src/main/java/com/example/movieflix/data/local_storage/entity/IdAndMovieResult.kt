package com.example.movieflix.data.local_storage.entity

import com.example.movieflix.domain.model.MovieResult

data class IdAndMovieResult(
    val id: Int,
    val movieResult: MovieResult,
)