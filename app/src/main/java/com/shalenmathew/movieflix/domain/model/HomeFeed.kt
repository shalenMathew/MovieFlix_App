package com.shalenmathew.movieflix.domain.model

data class HomeFeed(
    val title:String,
    val list:List<MovieResult>
)

data class HomeFeedData(
   val bannerMovie:List<MovieResult>,
   val homeFeedResponseList:List<HomeFeed>
)