package com.shalenmathew.movieflix.data.local_storage

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.shalenmathew.movieflix.core.utils.JsonParser
import com.shalenmathew.movieflix.data.model.HomeFeedResponse
import com.shalenmathew.movieflix.data.model.MovieResponseResult
import com.shalenmathew.movieflix.domain.model.MovieResult
import com.google.gson.reflect.TypeToken


@ProvidedTypeConverter
class MovieDataTypeConverter(private val jsonParser: JsonParser) {

    @TypeConverter
    fun movieResponseResultListToString(movieResponseResults: List<MovieResponseResult>): String {
        val listType = object : TypeToken<ArrayList<MovieResponseResult>>() {}.type
        return jsonParser.toJson(movieResponseResults, listType) ?: "[]"
    }

    @TypeConverter
    fun stringToMovieResponseResultList(data: String): List<MovieResponseResult> {
        val listType = object : TypeToken<ArrayList<MovieResponseResult>>() {}.type
        return jsonParser.fromJson<ArrayList<MovieResponseResult>>(
            data, listType
        ) ?: emptyList()
    }

    @TypeConverter
    fun homeFeedResponseListToString(homeFeedResponses: List<HomeFeedResponse>): String {
        val listType = object : TypeToken<ArrayList<HomeFeedResponse>>() {}.type
        return jsonParser.toJson(homeFeedResponses, listType) ?: "[]"
    }

    @TypeConverter
    fun stringToHomeFeedResponseList(data: String): List<HomeFeedResponse> {
        val listType = object : TypeToken<ArrayList<HomeFeedResponse>>() {}.type
        return jsonParser.fromJson<ArrayList<HomeFeedResponse>>(
            data, listType
        ) ?: emptyList()
    }

    @TypeConverter
    fun movieResultDataToStringData(movieResult: MovieResult): String {
        val listType = object : TypeToken<MovieResult>() {}.type
        return jsonParser.toJson(movieResult, listType) ?: "[]"
    }

    @TypeConverter
    fun stringToMovieResultData(data: String): MovieResult? {
        val listType = object : TypeToken<MovieResult>() {}.type
        return jsonParser.fromJson<MovieResult>(
            data, listType
        )
    }


}