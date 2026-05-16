package com.example.coursemate.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.coursemate.local.entity.HomeworkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework ORDER BY deadline IS NULL, deadline ASC, createdAt DESC")
    fun observeHomework(): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework WHERE courseId = :courseId ORDER BY deadline IS NULL, deadline ASC")
    fun observeHomeworkForCourse(courseId: Int): Flow<List<HomeworkEntity>>

    @Query("SELECT * FROM homework WHERE id = :homeworkId")
    fun observeHomework(homeworkId: Int): Flow<HomeworkEntity?>

    @Upsert
    suspend fun upsert(homework: HomeworkEntity)

    @Upsert
    suspend fun upsertAll(homework: List<HomeworkEntity>)

    @Query("DELETE FROM homework WHERE id = :homeworkId")
    suspend fun deleteById(homeworkId: Int)
}
