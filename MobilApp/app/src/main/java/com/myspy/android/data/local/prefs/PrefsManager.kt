package com.myspy.android.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Preferences Manager
 * @author Michael KOJDL
 * @version 1.0.0
 */
class PrefsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "myspy_secure_prefs"
        private const val KEY_UUID = "device_uuid"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_REGISTERED = "is_registered"
        private const val KEY_FIRST_RUN = "is_first_run"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback to regular SharedPreferences if encryption fails
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUuid(): String = prefs.getString(KEY_UUID, "") ?: ""
    fun saveUuid(uuid: String) = prefs.edit().putString(KEY_UUID, uuid).apply()

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "") ?: ""
    fun saveApiKey(apiKey: String) = prefs.edit().putString(KEY_API_KEY, apiKey).apply()

    fun isRegistered(): Boolean = prefs.getBoolean(KEY_REGISTERED, false)
    fun setRegistered(registered: Boolean) = prefs.edit().putBoolean(KEY_REGISTERED, registered).apply()

    fun isFirstRun(): Boolean = prefs.getBoolean(KEY_FIRST_RUN, true)
    fun setFirstRun(firstRun: Boolean) = prefs.edit().putBoolean(KEY_FIRST_RUN, firstRun).apply()

    fun clearAll() = prefs.edit().clear().apply()
}
