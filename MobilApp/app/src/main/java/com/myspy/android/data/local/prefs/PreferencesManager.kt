package com.myspy.android.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.systemcore.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences Manager - Encrypted SharedPreferences wrapper
 * @author Michael KOJDL
 * @version 1.0.0
 *
 * Manages all app settings and preferences with AES256-GCM encryption
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "myspy_secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ===== Server Configuration =====

    fun getApiBaseUrl(): String {
        return prefs.getString(KEY_API_BASE_URL, BuildConfig.API_BASE_URL) ?: BuildConfig.API_BASE_URL
    }

    fun setApiBaseUrl(url: String) {
        prefs.edit().putString(KEY_API_BASE_URL, url).apply()
    }

    fun getWssBaseUrl(): String {
        return prefs.getString(KEY_WSS_BASE_URL, BuildConfig.WSS_BASE_URL) ?: BuildConfig.WSS_BASE_URL
    }

    fun setWssBaseUrl(url: String) {
        prefs.edit().putString(KEY_WSS_BASE_URL, url).apply()
    }

    // ===== App Behavior =====

    fun isStealthModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_STEALTH_MODE, false)
    }

    fun setStealthModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STEALTH_MODE, enabled).apply()
    }

    fun isAutoStartEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_START, true)
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply()
    }

    // ===== System Hooks =====

    fun isSmsHookEnabled(): Boolean {
        return prefs.getBoolean(KEY_SMS_HOOK, true)
    }

    fun setSmsHookEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SMS_HOOK, enabled).apply()
    }

    fun isCallHookEnabled(): Boolean {
        return prefs.getBoolean(KEY_CALL_HOOK, true)
    }

    fun setCallHookEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CALL_HOOK, enabled).apply()
    }

    fun isLocationHookEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOCATION_HOOK, true)
    }

    fun setLocationHookEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION_HOOK, enabled).apply()
    }

    // ===== Device Registration =====

    fun getDeviceId(): String? {
        return prefs.getString(KEY_DEVICE_ID, null)
    }

    fun setDeviceId(deviceId: String?) {
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    fun setApiKey(apiKey: String?) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getRegistrationDate(): Long {
        return prefs.getLong(KEY_REGISTRATION_DATE, 0)
    }

    fun setRegistrationDate(timestamp: Long) {
        prefs.edit().putLong(KEY_REGISTRATION_DATE, timestamp).apply()
    }

    // ===== Sync Settings =====

    fun getSyncInterval(): Long {
        return prefs.getLong(KEY_SYNC_INTERVAL, DEFAULT_SYNC_INTERVAL)
    }

    fun setSyncInterval(intervalMs: Long) {
        prefs.edit().putLong(KEY_SYNC_INTERVAL, intervalMs).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }

    fun setLastSyncTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
    }

    // ===== Module Enable/Disable =====

    fun isModuleEnabled(moduleName: String): Boolean {
        return prefs.getBoolean("module_$moduleName", true)
    }

    fun setModuleEnabled(moduleName: String, enabled: Boolean) {
        prefs.edit().putBoolean("module_$moduleName", enabled).apply()
    }

    // ===== Clear All =====

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        // Server Configuration Keys
        private const val KEY_API_BASE_URL = "api_base_url"
        private const val KEY_WSS_BASE_URL = "wss_base_url"

        // App Behavior Keys
        private const val KEY_STEALTH_MODE = "stealth_mode"
        private const val KEY_AUTO_START = "auto_start"

        // System Hooks Keys
        private const val KEY_SMS_HOOK = "sms_hook"
        private const val KEY_CALL_HOOK = "call_hook"
        private const val KEY_LOCATION_HOOK = "location_hook"

        // Device Registration Keys
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_REGISTRATION_DATE = "registration_date"

        // Sync Settings Keys
        private const val KEY_SYNC_INTERVAL = "sync_interval"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"

        // Default Values
        private const val DEFAULT_SYNC_INTERVAL = 5 * 60 * 1000L // 5 minutes
    }
}
