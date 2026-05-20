package com.example.coursemate.network.dto

import com.google.gson.annotations.SerializedName

data class HomeworkCreateDto(
    @SerializedName("courseId")
    val courseId: Int,
    val title: String,
    val content: String,
    val deadline: String? = null,
    val status: String = "open"
)

data class HomeworkUpdateDto(
    @SerializedName("courseId")
    val courseId: Int? = null,
    val title: String? = null,
    val content: String? = null,
    val deadline: String? = null,
    val status: String? = null
)

data class HomeworkReadDto(
    val id: Int,
    @SerializedName("courseId")
    val courseId: Int,
    val title: String,
    val content: String,
    val deadline: String?,
    val status: String = "open",
    @SerializedName("createdAt")
    val createdAt: String
)

data class HomeworkSubmissionCreateDto(
    val content: String,
    @SerializedName("attachmentUrl")
    val attachmentUrl: String? = null
)

data class HomeworkSubmissionReadDto(
    val id: Int,
    @SerializedName("homeworkId")
    val homeworkId: Int,
    @SerializedName("studentId")
    val studentId: Int,
    val content: String,
    @SerializedName("attachmentUrl")
    val attachmentUrl: String?,
    @SerializedName("submittedAt")
    val submittedAt: String,
    val status: String
)
