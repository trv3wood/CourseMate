package com.example.coursemate.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "replies",
    indices = [Index(value = ["postId"])]
)
data class ReplyEntity(
    @PrimaryKey val id: Int,
    val postId: Int,
    val authorId: Int,
    val content: String,
    val isAccepted: Boolean,
    val createdAt: String
)
