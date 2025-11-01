package com.shalenmathew.movieflix.data.model

import com.shalenmathew.movieflix.domain.model.MediaVideoResultList

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