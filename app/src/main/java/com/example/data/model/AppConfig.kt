package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey
    val key: String = "primary_config",
    val ownerPin: String = "8327",
    val brandName: String = "BRIGHT DIGITAL",
    val contactPhone: String = "+91 83274 29438",
    val contactPhoneRaw: String = "+918327429438",
    val heroTitle: String = "BRIGHT DIGITAL",
    val heroSubtitle: String = "Cinematic Photography & Videography Portfolio",
    val globalDownloadsEnabled: Boolean = true,
    val watermarkEnabled: Boolean = false,
    val categoriesJson: String = "Wedding,Birthday,Annaprashan,Baby,Portrait,Nature"
) {
    fun getCategories(): List<String> {
        return categoriesJson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
