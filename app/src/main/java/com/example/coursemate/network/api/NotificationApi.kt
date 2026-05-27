package com.example.coursemate.network.api

import com.example.coursemate.network.dto.NotificationCreateDto
import com.example.coursemate.network.dto.NotificationReadDto
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {
    @GET("notifications")
    suspend fun listNotifications(): List<NotificationReadDto>

    @POST("notifications")
    suspend fun createNotification(
        @Body request: NotificationCreateDto
    ): NotificationReadDto
}
