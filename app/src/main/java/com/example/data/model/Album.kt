package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val coverUri: String,
    val description: String = "",
    val clientName: String = "",
    val eventDate: String = "",
    val itemCount: Int = 0,
    val featured: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
