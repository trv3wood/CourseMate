package com.example.coursemate.model

data class Post(
    val id: Int,
    val courseId: Int,
    val title: String,
    val content: String,
    val authorId: Int,
    val solved: Boolean,
    val createdAt: String
)

data class Reply(
    val id: Int,
    val postId: Int,
    val authorId: Int,
    val content: String,
    val isAccepted: Boolean,
    val createdAt: String
)

data class PostDetail(
    val post: Post,
    val replies: List<Reply>
)
