package com.example.movieflix.data.model

import com.example.movieflix.domain.model.MovieVideoResultList

data class MovieResponseVideoResultList(
    val id: Int?,
    val results: List<MovieResponseVideoResult>?
){
    fun toMovieVideoResultList():MovieVideoResultList{
        return MovieVideoResultList(
            id = id,
            results = results?.map {
            it.toMovieVideoResult()
        })
    }
}