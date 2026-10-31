package com.example.nutristrat.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

        private const val BASE_URL = "http://192.168.100.13/nutristrat_api/"

    val api: NutriStratApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NutriStratApi::class.java)
    }
}