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
    @SerializedName("timestamp") val timestamp: String
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

// Generic Response
data class ApiResponse(
    @SerializedName("result") val result: String,
    @SerializedName("created") val created: Int? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("message") val message: Any? = null
)
