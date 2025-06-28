package com.example.movieflix.data.model

import com.example.movieflix.domain.model.MovieResult

data class MovieResponseResult(
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: List<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int,
    val media_type: String
){
    // imagine this as a rough raw data we would get from api e would now filter this response by converting it to our Model class in domain
    // domain consist of  business logic so now we are molding the raw data according to our business logic

    fun toMovieResult():MovieResult{
        return MovieResult(
            backdropPath = backdrop_path,
            genreIds = genre_ids,
            id = id,
            originalLanguage = original_language,
            originalTitle = original_title,
            overview = overview,
            posterPath = poster_path,
            releaseDate = release_date,
            title = title,
            voteAverage = vote_average,
            mediaType = media_type)
    }

}