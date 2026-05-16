package com.example.coursemate.network.api

import com.example.coursemate.network.dto.CourseCreateDto
import com.example.coursemate.network.dto.CourseReadDto
import com.example.coursemate.network.dto.CourseUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseApi {
    @GET("courses")
    suspend fun listCourses(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<CourseReadDto>

    @POST("courses")
    suspend fun createCourse(@Body request: CourseCreateDto): CourseReadDto

    @GET("courses/{course_id}")
    suspend fun getCourse(@Path("course_id") courseId: Int): CourseReadDto

    @PUT("courses/{course_id}")
    suspend fun updateCourse(
        @Path("course_id") courseId: Int,
        @Body request: CourseUpdateDto
    ): CourseReadDto

    @DELETE("courses/{course_id}")
    suspend fun deleteCourse(@Path("course_id") courseId: Int): Response<Unit>
}
