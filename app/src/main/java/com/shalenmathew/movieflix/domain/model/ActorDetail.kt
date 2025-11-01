package com.shalenmathew.movieflix.domain.model

data class ActorDetail(
    val id: Int,
    val name: String,
    val biography: String?,
    val birthday: String?,
    val placeOfBirth: String?,
    val profilePath: String?,
    val backdropImagePath: String?,
    val knownForDepartment: String?,
    val instagramId: String?,
    val twitterId: String?,
    val facebookId: String?
)
