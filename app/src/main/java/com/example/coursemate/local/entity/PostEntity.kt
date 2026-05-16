package com.example.coursemate.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts",
    indices = [Index(value = ["courseId"])]
)
data class PostEntity(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val title: String,
    val content: String,
    val authorId: Int,
    val solved: Boolean,
    val createdAt: String
)
