package com.example.movieflix.data.model

import com.example.movieflix.domain.model.IN
import com.example.movieflix.domain.model.Results
import com.example.movieflix.domain.model.WatchProviders
import com.google.gson.annotations.SerializedName

data class WhereToWatchProviderResponse(
    val id: Int?=null,
    val results: ResultsResponse?=null
){
    fun toWatchProviders():WatchProviders{
        return WatchProviders(id = id, results = results?.toResults())
    }
}

data class ResultsResponse(
    var IN:INResponse?=null
){
    fun toResults():Results{
        return Results(IN = IN?.toIN())
    }
}

data class INResponse(
    var link:String?=null
){
    fun toIN():IN{
        return IN(link = link)
    }
}