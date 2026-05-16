package com.example.coursemate.repository

import com.example.coursemate.local.dao.CourseDao
import com.example.coursemate.model.Course
import com.example.coursemate.network.api.CourseApi
import com.example.coursemate.network.dto.CourseCreateDto
import com.example.coursemate.network.dto.CourseUpdateDto
import com.example.coursemate.utils.AppResult
import com.example.coursemate.utils.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepository(
    private val courseApi: CourseApi,
    private val courseDao: CourseDao
) {
    val courses: Flow<List<Course>> = courseDao.observeCourses().map { entities ->
        entities.map { it.toModel() }
    }

    fun observeCourse(courseId: Int): Flow<Course?> {
        return courseDao.observeCourse(courseId).map { it?.toModel() }
    }

    suspend fun refreshCourses(skip: Int = 0, limit: Int = 100): AppResult<Unit> = safeCall {
        val courses = courseApi.listCourses(skip = skip, limit = limit)
        courseDao.upsertAll(courses.map { it.toEntity() })
    }

    suspend fun getCourse(courseId: Int): AppResult<Course> = safeCall {
        val course = courseApi.getCourse(courseId)
        courseDao.upsert(course.toEntity())
        course.toEntity().toModel()
    }

    suspend fun createCourse(
        name: String,
        description: String?,
        teacherName: String
    ): AppResult<Course> = safeCall {
        val course = courseApi.createCourse(
            CourseCreateDto(
                name = name,
                description = description,
                teacherName = teacherName
            )
        )
        courseDao.upsert(course.toEntity())
        course.toEntity().toModel()
    }

    suspend fun updateCourse(
        courseId: Int,
        name: String? = null,
        description: String? = null,
        teacherName: String? = null
    ): AppResult<Course> = safeCall {
        val course = courseApi.updateCourse(
            courseId = courseId,
            request = CourseUpdateDto(
                name = name,
                description = description,
                teacherName = teacherName
            )
        )
        courseDao.upsert(course.toEntity())
        course.toEntity().toModel()
    }

    suspend fun deleteCourse(courseId: Int): AppResult<Unit> = safeCall {
        val response = courseApi.deleteCourse(courseId)
        if (!response.isSuccessful) {
            error("Delete course failed with HTTP ${response.code()}")
        }
        courseDao.deleteById(courseId)
    }
}
