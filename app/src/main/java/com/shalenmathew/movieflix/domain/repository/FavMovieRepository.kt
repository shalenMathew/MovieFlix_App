package com.shalenmathew.movieflix.domain.repository

import androidx.lifecycle.LiveData
import com.shalenmathew.movieflix.data.local_storage.entity.FavouritesEntity
import com.shalenmathew.movieflix.data.local_storage.entity.IdAndMovieResult

interface FavMovieRepository {
    suspend fun insertFavMovie(idAndMovieResult: IdAndMovieResult)

    suspend fun addPersonalNote(id: Int, personalNote: String?)

    suspend fun deleteFavMovie(favouritesEntity: FavouritesEntity)

    suspend fun updateFavMoviePoster(id: Int, posterPath: String)

    suspend fun updateFavMovieBanner(id: Int, bannerPath: String)

    suspend fun insertGalleryImage(image: com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity)

    suspend fun deleteGalleryImage(image: com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity)

    fun getGalleryImagesForMedia(mediaId: Int): LiveData<List<com.shalenmathew.movieflix.data.local_storage.entity.PersonalGalleryEntity>>

    fun getAllFavMovie(): LiveData<List<FavouritesEntity>>
}