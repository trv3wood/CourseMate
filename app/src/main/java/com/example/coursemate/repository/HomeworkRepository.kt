package com.example.coursemate.repository

import com.example.coursemate.local.dao.HomeworkDao
import com.example.coursemate.model.Homework
import com.example.coursemate.network.api.HomeworkApi
import com.example.coursemate.network.dto.HomeworkCreateDto
import com.example.coursemate.network.dto.HomeworkUpdateDto
import com.example.coursemate.utils.AppResult
import com.example.coursemate.utils.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeworkRepository(
    private val homeworkApi: HomeworkApi,
    private val homeworkDao: HomeworkDao
) {
    val homework: Flow<List<Homework>> = homeworkDao.observeHomework().map { entities ->
        entities.map { it.toModel() }
    }

    fun observeHomework(homeworkId: Int): Flow<Homework?> {
        return homeworkDao.observeHomework(homeworkId).map { it?.toModel() }
    }

    fun observeHomeworkForCourse(courseId: Int): Flow<List<Homework>> {
        return homeworkDao.observeHomeworkForCourse(courseId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun refreshHomework(skip: Int = 0, limit: Int = 100): AppResult<Unit> = safeCall {
        val homework = homeworkApi.listHomework(skip = skip, limit = limit)
        homeworkDao.upsertAll(homework.map { it.toEntity() })
    }

    suspend fun getHomework(homeworkId: Int): AppResult<Homework> = safeCall {
        val homework = homeworkApi.getHomework(homeworkId)
        homeworkDao.upsert(homework.toEntity())
        homework.toEntity().toModel()
    }

    suspend fun createHomework(
        courseId: Int,
        title: String,
        content: String,
        deadline: String?,
        status: String = "open"
    ): AppResult<Homework> = safeCall {
        val homework = homeworkApi.createHomework(
            HomeworkCreateDto(
                courseId = courseId,
                title = title,
                content = content,
                deadline = deadline,
                status = status
            )
        )
        homeworkDao.upsert(homework.toEntity())
        homework.toEntity().toModel()
    }

    suspend fun updateHomework(
        homeworkId: Int,
        courseId: Int? = null,
        title: String? = null,
        content: String? = null,
        deadline: String? = null,
        status: String? = null
    ): AppResult<Homework> = safeCall {
        val homework = homeworkApi.updateHomework(
            homeworkId = homeworkId,
            request = HomeworkUpdateDto(
                courseId = courseId,
                title = title,
                content = content,
                deadline = deadline,
                status = status
            )
        )
        homeworkDao.upsert(homework.toEntity())
        homework.toEntity().toModel()
    }

    suspend fun deleteHomework(homeworkId: Int): AppResult<Unit> = safeCall {
        val response = homeworkApi.deleteHomework(homeworkId)
        if (!response.isSuccessful) {
            error("Delete homework failed with HTTP ${response.code()}")
        }
        homeworkDao.deleteById(homeworkId)
    }
}
