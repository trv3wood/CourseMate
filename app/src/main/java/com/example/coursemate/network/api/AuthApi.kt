package com.example.coursemate.network.api

import com.example.coursemate.network.dto.LoginRequestDto
import com.example.coursemate.network.dto.RegisterRequestDto
import com.example.coursemate.network.dto.TokenDto
import com.example.coursemate.network.dto.UserReadDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): UserReadDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenDto
}
