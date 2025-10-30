package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.CallEntity

@Dao
interface CallsDao {
    @Query("SELECT * FROM call_logs WHERE synced = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUnsyncedCalls(limit: Int = 100): List<CallEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(call: CallEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calls: List<CallEntity>)

    @Query("UPDATE call_logs SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM call_logs WHERE synced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSynced(timestamp: Long)

    @Query("SELECT COUNT(*) FROM call_logs WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
}
