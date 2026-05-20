package com.example.coursemate.network.api

import com.example.coursemate.network.dto.PostCreateDto
import com.example.coursemate.network.dto.PostDetailDto
import com.example.coursemate.network.dto.PostReadDto
import com.example.coursemate.network.dto.PostUpdateDto
import com.example.coursemate.network.dto.ReplyCreateDto
import com.example.coursemate.network.dto.ReplyReadDto
import com.example.coursemate.network.dto.ReplyUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {
    @GET("posts")
    suspend fun listPosts(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 20
    ): List<PostReadDto>

    @POST("posts")
    suspend fun createPost(@Body request: PostCreateDto): PostReadDto

    @GET("posts/{post_id}")
    suspend fun getPost(@Path("post_id") postId: Int): PostDetailDto

    @PUT("posts/{post_id}")
    suspend fun updatePost(
        @Path("post_id") postId: Int,
        @Body request: PostUpdateDto
    ): PostReadDto

    @DELETE("posts/{post_id}")
    suspend fun deletePost(@Path("post_id") postId: Int): Response<Unit>

    @POST("posts/{post_id}/reply")
    suspend fun replyToPost(
        @Path("post_id") postId: Int,
        @Body request: ReplyCreateDto
    ): ReplyReadDto

    @PUT("reply/{reply_id}")
    suspend fun updateReply(
        @Path("reply_id") replyId: Int,
        @Body request: ReplyUpdateDto
    ): ReplyReadDto

    @DELETE("reply/{reply_id}")
    suspend fun deleteReply(@Path("reply_id") replyId: Int): Response<Unit>

    @POST("reply/{reply_id}/accept")
    suspend fun acceptReply(@Path("reply_id") replyId: Int): ReplyReadDto
}
