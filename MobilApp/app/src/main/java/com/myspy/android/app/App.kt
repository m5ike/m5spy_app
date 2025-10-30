package com.myspy.android.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application class - My Spy
 *
 * @author Michael KOJDL
 * @version 1.0.0
 * @revision 2025-10-30
 *
 * @description Hlavní Application class. Inicializuje DI, logging a notifikační kanály.
 *
 * Závislosti:
 * - Hilt (Dependency Injection)
 * - Timber (Logging)
 *
 * Poslední změny:
 * - 2025-10-30: Iniciální verze
 *
 * TODO:
 * - [ ] Přidat crash reporting (Firebase Crashlytics)
 * - [ ] Implementovat Analytics
 */
@HiltAndroidApp
class App : Application() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "myspy_service_channel"
        const val NOTIFICATION_CHANNEL_NAME = "System Service"

        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inicializace Timber loggeru
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("App onCreate")

        // Vytvoření notifikačního kanálu pro foreground service
        createNotificationChannel()

        // Inicializace služeb
        initializeServices()
    }

    /**
     * Vytvoří notifikační kanál pro Foreground Service
     * Požadováno pro Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service notification channel"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Timber.d("Notification channel created")
        }
    }

    /**
     * Inicializace služeb při startu aplikace
     */
    private fun initializeServices() {
        // Spustit hlavní službu pokud je aplikace již registrovaná
        // TODO: Implementovat check registrace a start MainService
        Timber.d("Services initialized")
    }
}
