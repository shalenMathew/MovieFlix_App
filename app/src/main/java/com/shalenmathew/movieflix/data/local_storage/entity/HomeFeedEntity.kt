package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shalenmathew.movieflix.core.utils.Constants
import com.shalenmathew.movieflix.data.model.HomeFeedResponse
import com.shalenmathew.movieflix.data.model.MovieResponseResult
import com.shalenmathew.movieflix.domain.model.HomeFeedData


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