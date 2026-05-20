package com.example.coursemate.viewmodel

import com.example.coursemate.local.database.CourseMateDatabase
import com.example.coursemate.network.api.HomeworkApi
import com.example.coursemate.network.dto.HomeworkCreateDto
import com.example.coursemate.network.dto.HomeworkReadDto
import com.example.coursemate.network.dto.HomeworkSubmissionCreateDto
import com.example.coursemate.network.dto.HomeworkSubmissionReadDto
import com.example.coursemate.network.dto.HomeworkUpdateDto
import com.example.coursemate.repository.HomeworkRepository
import com.example.coursemate.testing.MainDispatcherRule
import com.example.coursemate.testing.TestDatabaseFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [34])
class HomeworkViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: CourseMateDatabase

    @Before
    fun setUp() {
        database = TestDatabaseFactory.create()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun submitHomeworkDoesNotLoadTeacherSubmissionListByDefault() = runTest(mainDispatcherRule.testDispatcher) {
        val fakeApi = FakeHomeworkApi()
        val viewModel = HomeworkViewModel(
            HomeworkRepository(
                homeworkApi = fakeApi,
                homeworkDao = database.homeworkDao()
            )
        )

        advanceUntilIdle()

        viewModel.submitHomework(
            homeworkId = 1,
            content = "my answer",
            attachmentUrl = null
        )
        advanceUntilIdle()

        assertEquals(0, fakeApi.listHomeworkSubmissionsCalls)
        assertEquals(1, fakeApi.listMySubmissionsCalls)

    }

    private class FakeHomeworkApi : HomeworkApi {
        var listHomeworkSubmissionsCalls = 0
        var listMySubmissionsCalls = 0

        override suspend fun listHomework(skip: Int, limit: Int): List<HomeworkReadDto> = emptyList()

        override suspend fun createHomework(request: HomeworkCreateDto): HomeworkReadDto {
            error("Not used in this test")
        }

        override suspend fun getHomework(homeworkId: Int): HomeworkReadDto {
            error("Not used in this test")
        }

        override suspend fun updateHomework(homeworkId: Int, request: HomeworkUpdateDto): HomeworkReadDto {
            error("Not used in this test")
        }

        override suspend fun deleteHomework(homeworkId: Int): Response<Unit> {
            error("Not used in this test")
        }

        override suspend fun submitHomework(
            homeworkId: Int,
            request: HomeworkSubmissionCreateDto
        ): HomeworkSubmissionReadDto {
            return HomeworkSubmissionReadDto(
                id = 1,
                homeworkId = homeworkId,
                studentId = 7,
                content = request.content,
                attachmentUrl = request.attachmentUrl,
                submittedAt = "2026-05-20T12:00:00",
                status = "submitted"
            )
        }

        override suspend fun listHomeworkSubmissions(
            homeworkId: Int,
            skip: Int,
            limit: Int
        ): List<HomeworkSubmissionReadDto> {
            listHomeworkSubmissionsCalls += 1
            return emptyList()
        }

        override suspend fun listMySubmissions(skip: Int, limit: Int): List<HomeworkSubmissionReadDto> {
            listMySubmissionsCalls += 1
            return listOf(
                HomeworkSubmissionReadDto(
                    id = 1,
                    homeworkId = 1,
                    studentId = 7,
                    content = "my answer",
                    attachmentUrl = null,
                    submittedAt = "2026-05-20T12:00:00",
                    status = "submitted"
                )
            )
        }
    }
}
