package com.example.coursemate.repository

import app.cash.turbine.test
import com.example.coursemate.local.database.CourseMateDatabase
import com.example.coursemate.local.entity.CourseEntity
import com.example.coursemate.network.api.CourseApi
import com.example.coursemate.network.dto.CourseCreateDto
import com.example.coursemate.network.dto.CourseReadDto
import com.example.coursemate.network.dto.CourseUpdateDto
import com.example.coursemate.testing.TestDatabaseFactory
import com.example.coursemate.utils.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CourseRepositoryTest {
    private lateinit var database: CourseMateDatabase
    private lateinit var fakeApi: FakeCourseApi
    private lateinit var repository: CourseRepository

    @Before
    fun setUp() {
        database = TestDatabaseFactory.create()
        fakeApi = FakeCourseApi()
        repository = CourseRepository(fakeApi, database.courseDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshCoursesUpdatesLocalCache() = runTest {
        fakeApi.listCoursesResult = Result.success(
            listOf(
                CourseReadDto(
                    id = 10,
                    name = "Android Development",
                    description = "Compose basics",
                    teacherName = "Prof. Lin",
                    createdAt = "2026-05-16T08:00:00Z"
                )
            )
        )

        repository.courses.test {
            assertTrue(awaitItem().isEmpty())
            val result = repository.refreshCourses()

            assertTrue(result is AppResult.Success)
            val cached = awaitItem()
            assertEquals(1, cached.size)
            assertEquals("Android Development", cached.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshFailureKeepsOfflineCacheAvailable() = runTest {
        database.courseDao().upsert(
            CourseEntity(
                id = 20,
                name = "Cached Course",
                description = "Available offline",
                teacherName = "Prof. Chen",
                createdAt = "2026-05-15T08:00:00Z"
            )
        )
        fakeApi.listCoursesResult = Result.failure(IllegalStateException("network down"))

        repository.courses.test {
            assertEquals("Cached Course", awaitItem().first().name)

            val result = repository.refreshCourses()

            assertTrue(result is AppResult.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeCourseApi : CourseApi {
        var listCoursesResult: Result<List<CourseReadDto>> = Result.success(emptyList())

        override suspend fun listCourses(skip: Int, limit: Int): List<CourseReadDto> {
            return listCoursesResult.getOrThrow()
        }

        override suspend fun createCourse(request: CourseCreateDto): CourseReadDto {
            return CourseReadDto(
                id = 30,
                name = request.name,
                description = request.description,
                teacherName = request.teacherName,
                createdAt = "2026-05-16T09:00:00Z"
            )
        }

        override suspend fun getCourse(courseId: Int): CourseReadDto {
            return CourseReadDto(
                id = courseId,
                name = "Course $courseId",
                description = null,
                teacherName = "Teacher",
                createdAt = "2026-05-16T09:00:00Z"
            )
        }

        override suspend fun updateCourse(courseId: Int, request: CourseUpdateDto): CourseReadDto {
            return CourseReadDto(
                id = courseId,
                name = request.name ?: "Course $courseId",
                description = request.description,
                teacherName = request.teacherName ?: "Teacher",
                createdAt = "2026-05-16T09:00:00Z"
            )
        }

        override suspend fun deleteCourse(courseId: Int): Response<Unit> {
            return Response.success(Unit)
        }
    }
}
