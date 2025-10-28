package com.example.movieflix.domain.model



data class WatchProviders(
   val id:Int?=null,
   val results: Results?=null
)

data class Results(
    val IN:IN?=null
)

data class IN(
    val link:String ?=null,
    val flatrate: List<Provider>?=null,
    val buy: List<Provider>?=null,
    val rent: List<Provider>?=null
)

data class Provider(
    val logoPath: String?=null,
    val providerId: Int?=null,
    val providerName: String?=null
)