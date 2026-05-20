/**
 * UPDATED: The default backend is now pointed to the deployed Google Cloud Run instance.
 * This ensures that network requests point to the production-ready simulation server.
 */
package edu.skku.cs.citysmart.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    private const val BASE_URL = "https://citysmart-backend-147271219875.europe-west1.run.app/"
    private var currentBaseUrl = BASE_URL

    private var retrofit: Retrofit? = null

    private val okHttpClient = OkHttpClient.Builder()
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

        if (!formattedIp.startsWith("http://") && !formattedIp.startsWith("https://")) {
            formattedIp = "http://$formattedIp"
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
