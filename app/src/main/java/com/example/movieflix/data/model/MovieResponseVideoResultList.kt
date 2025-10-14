package com.example.movieflix.data.model

import com.example.movieflix.domain.model.MediaVideoResultList

data class MovieResponseVideoResultList(
    val id: Int?,
    val results: List<MovieResponseVideoResult>?
){
    fun toMovieVideoResultList():MediaVideoResultList{
        return MediaVideoResultList(
            id = id,
            results = results?.map {
            it.toMovieVideoResult()
        })
    }
}