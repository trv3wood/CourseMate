package com.example.coursemate.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.coursemate.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clear()
}
