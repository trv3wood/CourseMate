package com.example.coursemate.network.api

import com.example.coursemate.network.dto.UserReadDto
import retrofit2.http.GET

interface UserApi {
    @GET("users/me")
    suspend fun currentUser(): UserReadDto
}
