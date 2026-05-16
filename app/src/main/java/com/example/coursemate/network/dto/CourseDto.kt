package com.example.coursemate.network.dto

import com.google.gson.annotations.SerializedName

data class CourseCreateDto(
    val name: String,
    val description: String?,
    @SerializedName("teacherName")
    val teacherName: String
)

data class CourseUpdateDto(
    val name: String? = null,
    val description: String? = null,
    @SerializedName("teacherName")
    val teacherName: String? = null
)

data class CourseReadDto(
    val id: Int,
    val name: String,
    val description: String?,
    @SerializedName("teacherName")
    val teacherName: String,
    @SerializedName("createdAt")
    val createdAt: String
)
