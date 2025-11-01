package com.shalenmathew.movieflix.domain.model

data class CrewMember(
    val id: Int,
    val name: String,
    val job: String,
    val profilePath: String?
)
