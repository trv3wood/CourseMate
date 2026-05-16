package com.example.coursemate.local.dao

import app.cash.turbine.test
import com.example.coursemate.local.database.CourseMateDatabase
import com.example.coursemate.local.entity.HomeworkEntity
import com.example.coursemate.testing.TestDatabaseFactory
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeworkDaoTest {
    private lateinit var database: CourseMateDatabase
    private lateinit var dao: HomeworkDao

    @Before
    fun setUp() {
        database = TestDatabaseFactory.create()
        dao = database.homeworkDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertDeleteAndObserveHomework() = runTest {
        val homework = HomeworkEntity(
            id = 1,
            courseId = 10,
            title = "Essay",
            content = "Write summary",
            deadline = "2026-05-20T23:59:00Z",
            status = "open",
            createdAt = "2026-05-16T08:00:00Z"
        )

        dao.observeHomework().test {
            assertTrue(awaitItem().isEmpty())

            dao.upsert(homework)
            assertEquals("Essay", awaitItem().first().title)

            dao.upsert(homework.copy(title = "Updated essay", status = "submitted"))
            val updated = awaitItem().first()
            assertEquals("Updated essay", updated.title)
            assertEquals("submitted", updated.status)

            dao.deleteById(1)
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun queryHomeworkForCourseOnlyReturnsMatchingCourse() = runTest {
        dao.upsert(
            HomeworkEntity(
                id = 1,
                courseId = 10,
                title = "Math",
                content = "Practice",
                deadline = null,
                status = "open",
                createdAt = "2026-05-16T08:00:00Z"
            )
        )
        dao.upsert(
            HomeworkEntity(
                id = 2,
                courseId = 20,
                title = "English",
                content = "Read",
                deadline = null,
                status = "open",
                createdAt = "2026-05-16T08:05:00Z"
            )
        )

        dao.observeHomeworkForCourse(10).test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Math", items.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
