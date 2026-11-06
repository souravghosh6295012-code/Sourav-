package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.AlbumDao
import com.example.data.local.ConfigDao
import com.example.data.local.MediaDao
import com.example.data.model.Album
import com.example.data.model.AppConfig
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MediaRepository(
    private val mediaDao: MediaDao,
    private val albumDao: AlbumDao,
    private val configDao: ConfigDao
) {
    val allMedia: Flow<List<MediaItem>> = mediaDao.getAllMedia()
    val publicMedia: Flow<List<MediaItem>> = mediaDao.getPublicMedia()
    val publicVideos: Flow<List<MediaItem>> = mediaDao.getPublicMediaByType("VIDEO")
    val allAlbums: Flow<List<Album>> = albumDao.getAllAlbums()
    val appConfig: Flow<AppConfig?> = configDao.getConfig()

    fun getMediaByCategory(category: String): Flow<List<MediaItem>> {
        return mediaDao.getPublicMediaByCategory(category)
    }

    fun getMediaByAlbum(albumId: String): Flow<List<MediaItem>> {
        return mediaDao.getMediaByAlbum(albumId)
    }

    suspend fun getMediaCount(): Int = mediaDao.getMediaCount()

    suspend fun insertMedia(item: MediaItem) {
        mediaDao.insertMedia(item)
    }

    suspend fun updateMedia(item: MediaItem) {
        mediaDao.updateMedia(item)
    }

    suspend fun deleteMedia(item: MediaItem) {
        mediaDao.deleteMedia(item)
        // If it's a locally stored file, delete it from storage as well
        if (!item.uri.startsWith("http")) {
            try {
                val file = File(item.uri)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun insertAlbum(album: Album) {
        albumDao.insertAlbum(album)
    }

    suspend fun updateAlbum(album: Album) {
        albumDao.updateAlbum(album)
    }

    suspend fun deleteAlbum(album: Album) {
        albumDao.deleteAlbum(album)
    }

    suspend fun updateConfig(config: AppConfig) {
        configDao.updateConfig(config)
    }

    suspend fun getConfigOnce(): AppConfig {
        return configDao.getConfigOnce() ?: AppConfig()
    }

    suspend fun likeMedia(id: String) {
        mediaDao.incrementLikes(id)
    }

    suspend fun viewMedia(id: String) {
        mediaDao.incrementViews(id)
    }

    suspend fun importMediaFromPhone(
        context: Context,
        uris: List<Uri>,
        isVideo: Boolean,
        category: String,
        albumId: String,
        albumName: String,
        clientName: String,
        titlePrefix: String,
        description: String,
        allowDownload: Boolean,
        onProgress: (Int, Int) -> Unit
    ): Int = withContext(Dispatchers.IO) {
        val mediaDir = File(context.filesDir, "media").apply {
            if (!exists()) mkdirs()
        }

        var importedCount = 0
        uris.forEachIndexed { index, uri ->
            try {
                val ext = if (isVideo) ".mp4" else ".jpg"
                val destFile = File(mediaDir, "media_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}$ext")
                
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val title = if (uris.size == 1) {
                    titlePrefix.ifEmpty { if (isVideo) "Cinematic Video" else "Portrait Shot" }
                } else {
                    "${titlePrefix.ifEmpty { "Photo" }} #${index + 1}"
                }

                val mediaItem = MediaItem(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description,
                    mediaType = if (isVideo) "VIDEO" else "PHOTO",
                    uri = destFile.absolutePath,
                    thumbnailUri = if (isVideo) destFile.absolutePath else "",
                    category = category,
                    albumId = albumId,
                    albumName = albumName,
                    clientName = clientName,
                    shootDate = "Added recently",
                    isPublic = true,
                    allowDownload = allowDownload,
                    featured = index == 0
                )
                mediaDao.insertMedia(mediaItem)
                importedCount++
                withContext(Dispatchers.Main) {
                    onProgress(importedCount, uris.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        importedCount
    }
}
