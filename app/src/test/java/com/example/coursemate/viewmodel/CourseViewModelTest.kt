package com.example.coursemate.viewmodel

import app.cash.turbine.test
import app.cash.turbine.ReceiveTurbine
import com.example.coursemate.local.database.CourseMateDatabase
import com.example.coursemate.network.api.CourseApi
import com.example.coursemate.network.dto.CourseCreateDto
import com.example.coursemate.network.dto.CourseReadDto
import com.example.coursemate.network.dto.CourseUpdateDto
import com.example.coursemate.repository.CourseRepository
import com.example.coursemate.testing.MainDispatcherRule
import com.example.coursemate.testing.TestDatabaseFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
class CourseViewModelTest {
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
    fun initRefreshEmitsLoadingThenSuccessState() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = CourseViewModel(
            CourseRepository(
                courseApi = FakeCourseApi(
                    result = Result.success(
                        listOf(
                            CourseReadDto(
                                id = 1,
                                name = "Mobile Apps",
                                description = "Android course",
                                teacherName = "Prof. Wang",
                                createdAt = "2026-05-16T08:00:00Z"
                            )
                        )
                    )
                ),
                courseDao = database.courseDao()
            )
        )

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)

            runCurrent()
            awaitState { it.isLoading }

            advanceUntilIdle()
            val success = awaitState { !it.isLoading && it.courses.isNotEmpty() }
            assertEquals("Mobile Apps", success.courses.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initRefreshEmitsErrorState() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = CourseViewModel(
            CourseRepository(
                courseApi = FakeCourseApi(
                    result = Result.failure(IllegalStateException("server unavailable"))
                ),
                courseDao = database.courseDao()
            )
        )

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)

            runCurrent()
            awaitState { it.isLoading }

            advanceUntilIdle()
            val error = awaitState { !it.isLoading && it.errorMessage != null }
            assertEquals("server unavailable", error.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeCourseApi(
        private val result: Result<List<CourseReadDto>>
    ) : CourseApi {
        override suspend fun listCourses(skip: Int, limit: Int): List<CourseReadDto> {
            delay(50)
            return result.getOrThrow()
        }

        override suspend fun createCourse(request: CourseCreateDto): CourseReadDto {
            error("Not used in this test")
        }

        override suspend fun getCourse(courseId: Int): CourseReadDto {
            error("Not used in this test")
        }

        override suspend fun updateCourse(courseId: Int, request: CourseUpdateDto): CourseReadDto {
            error("Not used in this test")
        }

        override suspend fun deleteCourse(courseId: Int): Response<Unit> {
            error("Not used in this test")
        }
    }

    private suspend fun ReceiveTurbine<CourseUiState>.awaitState(
        predicate: (CourseUiState) -> Boolean
    ): CourseUiState {
        repeat(5) {
            val state = awaitItem()
            if (predicate(state)) {
                return state
            }
        }
        error("Expected CourseUiState was not emitted")
    }
}
