package com.example.coursemate.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.coursemate.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAt DESC")
    fun observePosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE id = :postId")
    fun observePost(postId: Int): Flow<PostEntity?>

    @Upsert
    suspend fun upsert(post: PostEntity)

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deleteById(postId: Int)
}
