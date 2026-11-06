package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val mediaType: String, // "PHOTO" or "VIDEO"
    val uri: String,
    val thumbnailUri: String = "",
    val category: String, // Wedding, Birthday, Annaprashan, Baby, Portrait, Nature
    val albumId: String = "",
    val albumName: String = "",
    val clientName: String = "",
    val shootDate: String = "",
    val isPublic: Boolean = true,
    val allowDownload: Boolean = true,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val featured: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
