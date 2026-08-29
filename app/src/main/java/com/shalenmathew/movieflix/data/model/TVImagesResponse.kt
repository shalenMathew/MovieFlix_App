package com.shalenmathew.movieflix.data.model

import com.google.gson.annotations.SerializedName

data class TVImagesResponse(
    val id: Int?,
    @SerializedName("backdrops") val backdrops: List<ImageDetailResponse>?,
    @SerializedName("posters") val posters: List<ImageDetailResponse>?
)

data class ImageDetailResponse(
    @SerializedName("aspect_ratio") val aspectRatio: Double?,
    @SerializedName("file_path") val filePath: String?,
    @SerializedName("height") val height: Int?,
    @SerializedName("width") val width: Int?
)
