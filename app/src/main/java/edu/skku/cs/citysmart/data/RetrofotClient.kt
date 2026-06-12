/**
 * UPDATED: BASE_URL changed to http://172.30.1.5:8000/ to point to the active simulation server.
 */
package edu.skku.cs.citysmart.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 🚀 Physical device LAN IP — host machine's WiFi address on the local network
    // NOTE: 10.0.2.2 only works in the Android Emulator. For a real phone, use the host's LAN IP.
    private const val BASE_URL = "http://172.30.1.5:8000/"

    val instance: DigitalTwinApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Automatically turns backend JSON strings into our Kotlin Objects
            .build()

        retrofit.create(DigitalTwinApiService::class.java)
    }
}