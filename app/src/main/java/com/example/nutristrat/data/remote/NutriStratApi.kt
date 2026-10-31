package com.example.nutristrat.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface NutriStratApi {

    @POST("register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse

    @POST("login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse
}