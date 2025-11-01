package com.shalenmathew.movieflix.domain.usecases

import com.shalenmathew.movieflix.domain.repository.ActorRepository
import javax.inject.Inject

class GetActorInfo @Inject constructor(
    private val actorRepository: ActorRepository
) {
    fun getActorDetail(personId: Int) = actorRepository.getActorDetail(personId)
    
    fun getActorMoviesAndShows(personId: Int) = actorRepository.getActorMoviesAndShows(personId)
}
