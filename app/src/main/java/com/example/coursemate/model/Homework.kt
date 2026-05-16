package com.example.coursemate.model

data class Homework(
    val id: Int,
    val courseId: Int,
    val title: String,
    val content: String,
    val deadline: String?,
    val status: String,
    val createdAt: String
)
