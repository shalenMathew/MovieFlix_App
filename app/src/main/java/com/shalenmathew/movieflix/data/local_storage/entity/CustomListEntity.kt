package com.shalenmathew.movieflix.data.local_storage.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_list_table")
data class CustomListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis()
)
