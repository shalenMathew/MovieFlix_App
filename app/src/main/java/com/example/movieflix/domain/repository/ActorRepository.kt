package com.example.movieflix.domain.repository

import com.example.movieflix.core.utils.NetworkResults
import com.example.movieflix.domain.model.ActorDetail
import com.example.movieflix.domain.model.MovieResult
import kotlinx.coroutines.flow.Flow

interface ActorRepository {
    fun getActorDetail(personId: Int): Flow<NetworkResults<ActorDetail>>
    fun getActorMoviesAndShows(personId: Int): Flow<NetworkResults<List<MovieResult>>>
}
