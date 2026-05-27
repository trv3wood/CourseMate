package com.example.coursemate.repository

import com.example.coursemate.model.Notification
import com.example.coursemate.network.api.NotificationApi
import com.example.coursemate.network.dto.NotificationCreateDto
import com.example.coursemate.utils.AppResult
import com.example.coursemate.utils.safeCall

class NotificationRepository(
    private val notificationApi: NotificationApi
) {
    suspend fun listNotifications(): AppResult<List<Notification>> = safeCall {
        notificationApi.listNotifications().map { it.toModel() }
    }

    suspend fun createNotification(
        title: String,
        message: String,
        type: String = "course"
    ): AppResult<Notification> = safeCall {
        notificationApi.createNotification(
            NotificationCreateDto(
                title = title,
                message = message,
                type = type
            )
        ).toModel()
    }
}
