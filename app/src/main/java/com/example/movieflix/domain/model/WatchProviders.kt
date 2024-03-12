package com.example.movieflix.domain.model



data class WatchProviders(
   val id:Int?=null,
   val results: Results?=null
)

data class Results(
    val IN:IN?=null
)

data class IN(
    val link:String ?=null
)