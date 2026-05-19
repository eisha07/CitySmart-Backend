package edu.skku.cs.citysmart.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // 🚨 IMPORTANT: Replace the IP address below with the actual local Wi-Fi IP
    // of the laptop your partner is running the FastAPI server on!
    private const val BASE_URL = "http://172.30.1.35:8080/"

    val api: CitySmartApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CitySmartApi::class.java)
    }
}