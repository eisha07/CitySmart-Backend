package edu.skku.cs.citysmart.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    private const val BASE_URL = "https://citysmart-backend-147271219875.europe-west1.run.app/"


    val api: CitySmartApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CitySmartApi::class.java)
    }
}