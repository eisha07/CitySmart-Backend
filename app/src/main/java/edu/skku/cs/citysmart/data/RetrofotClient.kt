/**
 * UPDATED: BASE_URL changed to http://172.30.1.5:8000/ to point to the active simulation server.
 */
package edu.skku.cs.citysmart.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 🚀 Updated to the active server IP and port
    private const val BASE_URL = "http://172.30.1.5:8000/"

    val instance: DigitalTwinApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Automatically turns backend JSON strings into our Kotlin Objects
            .build()

        retrofit.create(DigitalTwinApiService::class.java)
    }
}