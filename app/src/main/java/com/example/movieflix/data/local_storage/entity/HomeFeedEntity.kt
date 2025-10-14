package com.example.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.movieflix.core.utils.Constants
import com.example.movieflix.data.model.HomeFeedResponse
import com.example.movieflix.data.model.MovieResponseResult
import com.example.movieflix.domain.model.HomeFeedData


@Entity(Constants.TABLE_NAME)
data class HomeFeedEntity(
    @PrimaryKey
    val id:Int?=null,
    val bannerMovies:List<MovieResponseResult>,
    val homeFeedResponseList: List<HomeFeedResponse>
)
{
    fun toHomeFeedData(): HomeFeedData {
        return HomeFeedData(bannerMovies.map { it.toMovieResult()}
            ,homeFeedResponseList.map { it.toHomeFeed() })
    }
}