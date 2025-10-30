package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.MediaEntity

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(media: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(media: List<MediaEntity>)

    @Query("SELECT * FROM media_files WHERE synced = 0")
    suspend fun getUnsyncedMedia(): List<MediaEntity>

    @Query("SELECT COUNT(*) FROM media_files WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE media_files SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT * FROM media_files WHERE mediaType = :mediaType ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMediaByType(mediaType: String, limit: Int = 100): List<MediaEntity>

    @Query("SELECT * FROM media_files ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMedia(limit: Int = 100): List<MediaEntity>

    @Query("DELETE FROM media_files WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Delete
    suspend fun delete(media: MediaEntity)
}
