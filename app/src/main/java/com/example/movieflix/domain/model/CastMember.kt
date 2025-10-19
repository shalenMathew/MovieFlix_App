package com.example.movieflix.domain.model

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?,
    val instagramId: String? = null,
    val twitterId: String? = null,
    val facebookId: String? = null
)
