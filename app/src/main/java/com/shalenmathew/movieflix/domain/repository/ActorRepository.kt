package com.shalenmathew.movieflix.domain.repository

import com.shalenmathew.movieflix.core.utils.NetworkResults
import com.shalenmathew.movieflix.domain.model.ActorDetail
import com.shalenmathew.movieflix.domain.model.MovieResult
import kotlinx.coroutines.flow.Flow

interface ActorRepository {
    fun getActorDetail(personId: Int): Flow<NetworkResults<ActorDetail>>
    fun getActorMoviesAndShows(personId: Int): Flow<NetworkResults<List<MovieResult>>>
}
