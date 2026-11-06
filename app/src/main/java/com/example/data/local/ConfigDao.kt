package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_config WHERE `key` = 'primary_config' LIMIT 1")
    fun getConfig(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE `key` = 'primary_config' LIMIT 1")
    suspend fun getConfigOnce(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)

    @Update
    suspend fun updateConfig(config: AppConfig)
}
