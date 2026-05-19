package edu.skku.cs.citysmart.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 🚀 STEP 18: Replace this with your actual Mac IP address (Keep the http:// and the :8080/)
    private const val BASE_URL = "http://172.30.1.82:8080/"

    val instance: DigitalTwinApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Automatically turns backend JSON strings into our Kotlin Objects
            .build()

        retrofit.create(DigitalTwinApiService::class.java)
    }
}