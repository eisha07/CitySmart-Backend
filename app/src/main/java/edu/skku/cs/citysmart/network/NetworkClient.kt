/**
 * UPDATED: The default backend IP address is set to 172.30.1.5:8000.
 * This ensures that all network requests generated via this client point to the active simulation server.
 */
package edu.skku.cs.citysmart.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    // Default fallback set to the active server IP
    private var currentBaseUrl = "http://172.30.1.5:8000/"

    private var retrofit: Retrofit? = null

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
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
     * Dynamically updates the server address at runtime.
     * Expects format like "172.30.1.5:8000"
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
        buildRetrofit() // Re-initialize Retrofit with the new address
    }

    private fun buildRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}