package com.example.coursemate.network.dto

data class NotificationCreateDto(
    val title: String,
    val message: String,
    val type: String = "course"
)

data class NotificationReadDto(
    val id: Int,
    val title: String,
    val message: String,
    val type: String
)
