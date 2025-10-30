package com.myspy.android.data.local.db.dao

import androidx.room.*
import com.myspy.android.data.local.db.entities.AppEntity

@Dao
interface AppsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: AppEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(apps: List<AppEntity>)

    @Query("SELECT * FROM apps WHERE synced = 0")
    suspend fun getUnsyncedApps(): List<AppEntity>

    @Query("SELECT COUNT(*) FROM apps WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("UPDATE apps SET synced = 1 WHERE packageName IN (:packageNames)")
    suspend fun markAsSynced(packageNames: List<String>)

    @Query("SELECT * FROM apps WHERE packageName = :packageName")
    suspend fun getAppByPackage(packageName: String): AppEntity?

    @Query("SELECT * FROM apps ORDER BY appName ASC")
    suspend fun getAllApps(): List<AppEntity>

    @Query("SELECT * FROM apps WHERE isSystemApp = 0 ORDER BY appName ASC")
    suspend fun getUserApps(): List<AppEntity>

    @Query("DELETE FROM apps")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(app: AppEntity)
}
