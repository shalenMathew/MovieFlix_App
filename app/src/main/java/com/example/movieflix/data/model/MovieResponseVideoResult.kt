package com.example.movieflix.data.model

import com.example.movieflix.domain.model.MovieVideoResult

data class MovieResponseVideoResult(
    val id: String?,
    val iso_3166_1: String?,
    val iso_639_1: String?,
    val key: String?,
    val name: String?,
    val official: Boolean?,
    val published_at: String?,
    val site: String?,
    val size: Int?,
    val type: String?
){
    fun toMovieVideoResult():MovieVideoResult{
        return MovieVideoResult(id=id,
            key = key,
            name=name,
            site=site,
            type=type)
    }
}