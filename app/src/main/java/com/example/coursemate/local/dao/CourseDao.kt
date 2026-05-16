package com.example.coursemate.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.coursemate.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY createdAt DESC")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourse(courseId: Int): Flow<CourseEntity?>

    @Upsert
    suspend fun upsert(course: CourseEntity)

    @Upsert
    suspend fun upsertAll(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Int)
}
