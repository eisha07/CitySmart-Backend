package edu.skku.cs.citysmart.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    // 🚨 Physical device LAN IP — host machine's WiFi address on the local network
    // NOTE: 10.0.2.2 only works in the Android Emulator. For a real phone, use the host's LAN IP.
    private const val BASE_URL = "http://172.30.1.5:8000/"


    val api: CitySmartApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CitySmartApi::class.java)
    }
}