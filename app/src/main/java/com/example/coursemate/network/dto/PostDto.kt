package com.example.coursemate.network.dto

import com.google.gson.annotations.SerializedName

data class PostCreateDto(
    @SerializedName("courseId")
    val courseId: Int,
    val title: String,
    val content: String
)

data class PostReadDto(
    val id: Int,
    @SerializedName("courseId")
    val courseId: Int,
    val title: String,
    val content: String,
    @SerializedName("authorId")
    val authorId: Int,
    val solved: Boolean,
    @SerializedName("createdAt")
    val createdAt: String
)

data class PostDetailDto(
    val id: Int,
    @SerializedName("courseId")
    val courseId: Int,
    val title: String,
    val content: String,
    @SerializedName("authorId")
    val authorId: Int,
    val solved: Boolean,
    @SerializedName("createdAt")
    val createdAt: String,
    val replies: List<ReplyReadDto> = emptyList()
)

data class ReplyCreateDto(
    val content: String
)

data class ReplyReadDto(
    val id: Int,
    @SerializedName("postId")
    val postId: Int,
    @SerializedName("authorId")
    val authorId: Int,
    val content: String,
    @SerializedName("isAccepted")
    val isAccepted: Boolean,
    @SerializedName("createdAt")
    val createdAt: String
)
