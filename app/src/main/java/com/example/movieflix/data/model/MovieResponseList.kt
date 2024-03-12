package com.example.movieflix.data.model

import com.example.movieflix.domain.model.MovieList

// when i call the api the result will be saved in the form of this model class
data class MovieResponseList(

    val page: Int?,
    val results: List<MovieResponseResult>?,
//    val movieResponseResult: List<MovieResponseResult>?
    val total_pages: Int?,
    val total_results: Int?
    // this all parameters names are case sensitive if there will be small issue the json wont parse it
    // earlier i wasnt getting the list of movies as i modified the results name in json to 'movieResponseResult' in my data
    // class due to which the list was not parsing
){

    fun toMovieList():MovieList{
        return MovieList(results!!.map {
            // in toMovieResult() we r removing all extra parameters
            // converting the raw json model according to our business model and saving it in MovieList
            it.toMovieResult()
        })
    }

}