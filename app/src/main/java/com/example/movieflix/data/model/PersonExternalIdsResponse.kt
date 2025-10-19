package com.example.movieflix.data.model

import com.google.gson.annotations.SerializedName

data class PersonExternalIdsResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("freebase_mid")
    val freebaseMid: String?,
    @SerializedName("freebase_id")
    val freebaseId: String?,
    @SerializedName("imdb_id")
    val imdbId: String?,
    @SerializedName("tvrage_id")
    val tvrageId: Int?,
    @SerializedName("wikidata_id")
    val wikidataId: String?,
    @SerializedName("facebook_id")
    val facebookId: String?,
    @SerializedName("instagram_id")
    val instagramId: String?,
    @SerializedName("tiktok_id")
    val tiktokId: String?,
    @SerializedName("twitter_id")
    val twitterId: String?,
    @SerializedName("youtube_id")
    val youtubeId: String?
)
