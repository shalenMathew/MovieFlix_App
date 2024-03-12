package com.example.movieflix.data.model

import com.example.movieflix.domain.model.HomeFeed
import com.example.movieflix.domain.model.HomeFeedData


// this both class acts as abstraction converting the dat model class into domain data class

data class HomeFeedDataResponse(
    val bannerMovies:List<MovieResponseResult>, // storing the raw response from the api
    val homeFeedResponseList: List<HomeFeedResponse> // storing the raw response from the api
){
    fun toHomeFeedData():HomeFeedData{
       return HomeFeedData(bannerMovies.map { it.toMovieResult()} // converting the response into our model class (think our model class of a business as
           // filter i.e only important attributes to the business is stored in the model class )
           ,homeFeedResponseList.map { it.toHomeFeed() })
    }
}


data class HomeFeedResponse(
    val title:String,
    val list:List<MovieResponseResult>
){

    fun toHomeFeed():HomeFeed{
        return HomeFeed(title = title, list = list.map {
            it.toMovieResult()
        })
    }

}