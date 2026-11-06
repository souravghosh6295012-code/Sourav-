package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE isPublic = 1 ORDER BY timestamp DESC")
    fun getPublicMedia(): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type AND isPublic = 1 ORDER BY timestamp DESC")
    fun getPublicMediaByType(type: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE category = :category AND isPublic = 1 ORDER BY timestamp DESC")
    fun getPublicMediaByCategory(category: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE albumId = :albumId AND isPublic = 1 ORDER BY timestamp DESC")
    fun getMediaByAlbum(albumId: String): Flow<List<MediaItem>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): MediaItem?

    @Query("SELECT COUNT(*) FROM media_items")
    suspend fun getMediaCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)

    @Update
    suspend fun updateMedia(item: MediaItem)

    @Delete
    suspend fun deleteMedia(item: MediaItem)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE media_items SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementLikes(id: String)

    @Query("UPDATE media_items SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)
}
