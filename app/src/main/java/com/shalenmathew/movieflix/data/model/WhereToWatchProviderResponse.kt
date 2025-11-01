package com.shalenmathew.movieflix.data.model

import com.shalenmathew.movieflix.domain.model.IN
import com.shalenmathew.movieflix.domain.model.Provider
import com.shalenmathew.movieflix.domain.model.Results
import com.shalenmathew.movieflix.domain.model.WatchProviders
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
    var link:String?=null,
    var flatrate: List<ProviderResponse>?=null,
    var buy: List<ProviderResponse>?=null,
    var rent: List<ProviderResponse>?=null
){
    fun toIN():IN{
        return IN(
            link = link,
            flatrate = flatrate?.map { it.toProvider() },
            buy = buy?.map { it.toProvider() },
            rent = rent?.map { it.toProvider() }
        )
    }
}

data class ProviderResponse(
    @SerializedName("logo_path")
    val logoPath: String?=null,
    @SerializedName("provider_id")
    val providerId: Int?=null,
    @SerializedName("provider_name")
    val providerName: String?=null
){
    fun toProvider(): Provider {
        return Provider(
            logoPath = logoPath,
            providerId = providerId,
            providerName = providerName
        )
    }
}