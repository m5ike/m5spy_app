package com.myspy.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myspy.android.data.local.prefs.PrefsManager
import com.myspy.android.services.MainService

/**
 * Boot Receiver - Auto-start service on boot
 * @author Michael KOJDL
 * @version 1.0.0
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefsManager = PrefsManager(context)
            if (prefsManager.isRegistered()) {
                MainService.start(context)
            }
        }
    }
}
