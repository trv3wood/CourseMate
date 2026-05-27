package com.example.coursemate.utils

import com.example.coursemate.model.HomeworkSubmission
import com.example.coursemate.model.Post
import com.example.coursemate.model.Reply
import com.example.coursemate.model.User

private fun User.hasRole(vararg roles: String): Boolean {
    return roles.any { role.equals(it, ignoreCase = true) }
}

fun User.canManageCourses(): Boolean = hasRole("teacher", "admin")

fun User.canManageHomework(): Boolean = hasRole("teacher", "admin")

fun User.canPublishNotifications(): Boolean = hasRole("teacher", "admin")

fun User.canSubmitHomework(): Boolean = hasRole("student", "admin")

fun User.canManagePost(post: Post): Boolean {
    return hasRole("teacher", "admin") || id == post.authorId
}

fun User.canManageReply(reply: Reply): Boolean {
    return hasRole("teacher", "admin") || id == reply.authorId
}

fun User.canAcceptReply(post: Post): Boolean {
    return hasRole("teacher", "admin") || id == post.authorId
}

fun User.canViewHomeworkSubmissions(): Boolean = hasRole("teacher", "admin")

fun User.ownsSubmission(submission: HomeworkSubmission): Boolean = id == submission.studentId
