package com.example.coursemate.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.coursemate.local.entity.ReplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyDao {
    @Query("SELECT * FROM replies WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeReplies(postId: Int): Flow<List<ReplyEntity>>

    @Upsert
    suspend fun upsert(reply: ReplyEntity)

    @Upsert
    suspend fun upsertAll(replies: List<ReplyEntity>)

    @Query("DELETE FROM replies WHERE postId = :postId")
    suspend fun deleteForPost(postId: Int)

    @Query("DELETE FROM replies WHERE id = :replyId")
    suspend fun deleteById(replyId: Int)
}
