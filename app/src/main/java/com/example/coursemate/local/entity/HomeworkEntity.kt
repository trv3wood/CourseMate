package com.example.coursemate.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "homework",
    indices = [Index(value = ["courseId"])]
)
data class HomeworkEntity(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val title: String,
    val content: String,
    val deadline: String?,
    val status: String,
    val createdAt: String
)
