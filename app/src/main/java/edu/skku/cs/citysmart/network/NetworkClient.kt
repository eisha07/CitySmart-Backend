/**
 * UPDATED: Added HttpLoggingInterceptor to catch 500 error tracebacks.
 * Default scheme updated to HTTPS for secure cloud deployments.
 */
package edu.skku.cs.citysmart.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://citysmart-backend-1.onrender.com/"
    private var currentBaseUrl = BASE_URL

    private var retrofit: Retrofit? = null

    // 🔍 NEW: The X-Ray logger to catch server crashes
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // <-- Attached here
        .connectTimeout(60, TimeUnit.SECONDS) // Crucial for long AI generation times
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: CitySmartApi
        get() {
            if (retrofit == null) {
                buildRetrofit()
            }
            return retrofit!!.create(CitySmartApi::class.java)
        }

    /**
     * Dynamically updates the server address at runtime if needed (e.g., local testing).
     */
    fun updateBaseUrl(newIp: String) {
        var formattedIp = newIp.trim()
        if (formattedIp.isEmpty()) return

        // 🛡️ UPDATED: Default to HTTPS to satisfy Render's strict security rules
        if (!formattedIp.startsWith("http://") && !formattedIp.startsWith("https://")) {
            formattedIp = "https://$formattedIp"
        }
        if (!formattedIp.endsWith("/")) {
            formattedIp = "$formattedIp/"
        }

        currentBaseUrl = formattedIp
        buildRetrofit()
    }

    private fun buildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}