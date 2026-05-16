package com.example.coursemate.network.api

import com.example.coursemate.network.dto.NotificationReadDto
import retrofit2.http.GET

interface NotificationApi {
    @GET("notifications")
    suspend fun listNotifications(): List<NotificationReadDto>
}
