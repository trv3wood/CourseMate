package com.example.coursemate.model

data class Course(
    val id: Int,
    val name: String,
    val description: String?,
    val teacherName: String,
    val createdAt: String
)
