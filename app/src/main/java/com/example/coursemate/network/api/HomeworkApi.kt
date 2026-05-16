package com.example.coursemate.network.api

import com.example.coursemate.network.dto.HomeworkCreateDto
import com.example.coursemate.network.dto.HomeworkReadDto
import com.example.coursemate.network.dto.HomeworkUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeworkApi {
    @GET("homework")
    suspend fun listHomework(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<HomeworkReadDto>

    @POST("homework")
    suspend fun createHomework(@Body request: HomeworkCreateDto): HomeworkReadDto

    @GET("homework/{homework_id}")
    suspend fun getHomework(@Path("homework_id") homeworkId: Int): HomeworkReadDto

    @PUT("homework/{homework_id}")
    suspend fun updateHomework(
        @Path("homework_id") homeworkId: Int,
        @Body request: HomeworkUpdateDto
    ): HomeworkReadDto

    @DELETE("homework/{homework_id}")
    suspend fun deleteHomework(@Path("homework_id") homeworkId: Int): Response<Unit>
}
