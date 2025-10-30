package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.BrowserHistoryEntity

@Dao
interface BrowserHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: BrowserHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(history: List<BrowserHistoryEntity>)

    @Query("SELECT * FROM browser_history WHERE synced = 0")
    suspend fun getUnsyncedHistory(): List<BrowserHistoryEntity>

    @Query("SELECT COUNT(*) FROM browser_history WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE browser_history SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT * FROM browser_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int = 100): List<BrowserHistoryEntity>

    @Query("DELETE FROM browser_history WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Delete
    suspend fun delete(history: BrowserHistoryEntity)
}
