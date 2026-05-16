package com.example.coursemate.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    val username: String,
    val password: String
)

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "student"
)

data class TokenDto(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("tokenType")
    val tokenType: String = "bearer"
)

data class UserReadDto(
    val id: Int,
    val username: String,
    val email: String,
    val role: String = "student",
    @SerializedName("createdAt")
    val createdAt: String
)
