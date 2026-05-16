package com.example.coursemate.repository

import androidx.room.withTransaction
import com.example.coursemate.local.dao.PostDao
import com.example.coursemate.local.dao.ReplyDao
import com.example.coursemate.local.database.CourseMateDatabase
import com.example.coursemate.model.Post
import com.example.coursemate.model.PostDetail
import com.example.coursemate.model.Reply
import com.example.coursemate.network.api.PostApi
import com.example.coursemate.network.dto.PostCreateDto
import com.example.coursemate.network.dto.ReplyCreateDto
import com.example.coursemate.utils.AppResult
import com.example.coursemate.utils.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PostRepository(
    private val postApi: PostApi,
    private val postDao: PostDao,
    private val replyDao: ReplyDao,
    private val database: CourseMateDatabase
) {
    val posts: Flow<List<Post>> = postDao.observePosts().map { entities ->
        entities.map { it.toModel() }
    }

    fun observePost(postId: Int): Flow<Post?> {
        return postDao.observePost(postId).map { it?.toModel() }
    }

    fun observeReplies(postId: Int): Flow<List<Reply>> {
        return replyDao.observeReplies(postId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun observePostDetail(postId: Int): Flow<PostDetail?> {
        return combine(
            observePost(postId),
            observeReplies(postId)
        ) { post, replies ->
            post?.let { PostDetail(post = it, replies = replies) }
        }
    }

    suspend fun refreshPosts(skip: Int = 0, limit: Int = 100): AppResult<Unit> = safeCall {
        val posts = postApi.listPosts(skip = skip, limit = limit)
        postDao.upsertAll(posts.map { it.toEntity() })
    }

    suspend fun refreshPostDetail(postId: Int): AppResult<PostDetail> = safeCall {
        val detail = postApi.getPost(postId)
        database.withTransaction {
            postDao.upsert(detail.toEntity())
            replyDao.deleteForPost(postId)
            replyDao.upsertAll(detail.replies.map { it.toEntity() })
        }
        PostDetail(
            post = detail.toEntity().toModel(),
            replies = detail.replies.map { it.toEntity().toModel() }
        )
    }

    suspend fun createPost(courseId: Int, title: String, content: String): AppResult<Post> = safeCall {
        val post = postApi.createPost(
            PostCreateDto(
                courseId = courseId,
                title = title,
                content = content
            )
        )
        postDao.upsert(post.toEntity())
        post.toEntity().toModel()
    }

    suspend fun replyToPost(postId: Int, content: String): AppResult<Reply> = safeCall {
        val reply = postApi.replyToPost(
            postId = postId,
            request = ReplyCreateDto(content = content)
        )
        replyDao.upsert(reply.toEntity())
        reply.toEntity().toModel()
    }

    suspend fun acceptReply(replyId: Int): AppResult<Reply> = safeCall {
        val reply = postApi.acceptReply(replyId)
        replyDao.upsert(reply.toEntity())
        reply.toEntity().toModel()
    }
}
