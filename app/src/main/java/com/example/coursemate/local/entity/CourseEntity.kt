package com.example.coursemate.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String?,
    val teacherName: String,
    val createdAt: String
)
