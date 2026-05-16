package com.example.coursemate.repository

import com.example.coursemate.local.entity.CourseEntity
import com.example.coursemate.local.entity.HomeworkEntity
import com.example.coursemate.local.entity.PostEntity
import com.example.coursemate.local.entity.ReplyEntity
import com.example.coursemate.local.entity.UserEntity
import com.example.coursemate.model.Course
import com.example.coursemate.model.Homework
import com.example.coursemate.model.Notification
import com.example.coursemate.model.Post
import com.example.coursemate.model.Reply
import com.example.coursemate.model.User
import com.example.coursemate.network.dto.CourseReadDto
import com.example.coursemate.network.dto.HomeworkReadDto
import com.example.coursemate.network.dto.NotificationReadDto
import com.example.coursemate.network.dto.PostDetailDto
import com.example.coursemate.network.dto.PostReadDto
import com.example.coursemate.network.dto.ReplyReadDto
import com.example.coursemate.network.dto.UserReadDto

internal fun UserReadDto.toEntity() = UserEntity(
    id = id,
    username = username,
    email = email,
    role = role,
    createdAt = createdAt
)

internal fun UserEntity.toModel() = User(
    id = id,
    username = username,
    email = email,
    role = role,
    createdAt = createdAt
)

internal fun CourseReadDto.toEntity() = CourseEntity(
    id = id,
    name = name,
    description = description,
    teacherName = teacherName,
    createdAt = createdAt
)

internal fun CourseEntity.toModel() = Course(
    id = id,
    name = name,
    description = description,
    teacherName = teacherName,
    createdAt = createdAt
)

internal fun HomeworkReadDto.toEntity() = HomeworkEntity(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    deadline = deadline,
    status = status,
    createdAt = createdAt
)

internal fun HomeworkEntity.toModel() = Homework(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    deadline = deadline,
    status = status,
    createdAt = createdAt
)

internal fun PostReadDto.toEntity() = PostEntity(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    authorId = authorId,
    solved = solved,
    createdAt = createdAt
)

internal fun PostDetailDto.toEntity() = PostEntity(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    authorId = authorId,
    solved = solved,
    createdAt = createdAt
)

internal fun PostEntity.toModel() = Post(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    authorId = authorId,
    solved = solved,
    createdAt = createdAt
)

internal fun ReplyReadDto.toEntity() = ReplyEntity(
    id = id,
    postId = postId,
    authorId = authorId,
    content = content,
    isAccepted = isAccepted,
    createdAt = createdAt
)

internal fun ReplyEntity.toModel() = Reply(
    id = id,
    postId = postId,
    authorId = authorId,
    content = content,
    isAccepted = isAccepted,
    createdAt = createdAt
)

internal fun NotificationReadDto.toModel() = Notification(
    id = id,
    title = title,
    message = message,
    type = type
)
