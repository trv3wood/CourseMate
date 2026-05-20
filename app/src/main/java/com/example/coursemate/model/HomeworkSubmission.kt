package com.example.coursemate.model

data class HomeworkSubmission(
    val id: Int,
    val homeworkId: Int,
    val studentId: Int,
    val content: String,
    val attachmentUrl: String?,
    val submittedAt: String,
    val status: String
)
