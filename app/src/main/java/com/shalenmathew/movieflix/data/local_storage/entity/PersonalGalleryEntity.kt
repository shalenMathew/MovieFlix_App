package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "personal_gallery_table",
    foreignKeys = [
        ForeignKey(
            entity = FavouritesEntity::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaId"])]
)
data class PersonalGalleryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mediaId: Int,
    val imagePath: String,
    val addedAt: Long = System.currentTimeMillis()
)
