package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.LocationEntity

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE synced = 0 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getUnsyncedLocations(limit: Int = 100): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Query("UPDATE locations SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM locations WHERE synced = 1 AND createdAt < :timestamp")
    suspend fun deleteOldSynced(timestamp: Long)

    @Query("SELECT COUNT(*) FROM locations WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
}
