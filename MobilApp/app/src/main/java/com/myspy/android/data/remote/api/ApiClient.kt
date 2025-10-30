package com.myspy.android.data.remote.api

import com.myspy.android.core.constants.ApiConstants
import com.myspy.android.data.local.prefs.PrefsManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * API Client
 * @author Michael KOJDL
 * @version 1.0.0
 */
object ApiClient {

    fun createService(prefsManager: PrefsManager): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val uuid = prefsManager.getUuid()
            val apiKey = prefsManager.getApiKey()

            val request = chain.request().newBuilder()
                .addHeader(ApiConstants.HEADER_UUID, uuid)
                .addHeader(ApiConstants.HEADER_API_KEY, apiKey)
                .addHeader(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                .build()

            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(ApiConstants.TIMEOUT_CONNECT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.TIMEOUT_READ, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.TIMEOUT_WRITE, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiConstants.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
