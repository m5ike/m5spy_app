package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.LocationDao
import com.myspy.android.data.local.db.entities.LocationEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.LocationData
import java.text.SimpleDateFormat
import java.util.*

class LocationRepository(
    private val locationDao: LocationDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveLocation(location: LocationEntity): Long = locationDao.insert(location)

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = locationDao.getUnsyncedLocations()
            if (unsynced.isEmpty()) return Result.success(0)

            val dataList = unsynced.map { entity ->
                LocationData(
                    latitude = entity.latitude,
                    longitude = entity.longitude,
                    accuracy = entity.accuracy,
                    altitude = entity.altitude,
                    speed = entity.speed,
                    address = entity.address,
                    timestamp = dateFormat.format(Date(entity.timestamp))
                )
            }

            val response = apiService.uploadLocation(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                locationDao.markAsSynced(unsynced.map { it.id })
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int = locationDao.getUnsyncedCount()
}
