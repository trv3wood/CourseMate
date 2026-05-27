package com.example.coursemate.viewmodel

import app.cash.turbine.test
import com.example.coursemate.network.api.NotificationApi
import com.example.coursemate.network.dto.NotificationCreateDto
import com.example.coursemate.network.dto.NotificationReadDto
import com.example.coursemate.repository.NotificationRepository
import com.example.coursemate.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun createNotificationRefreshesListAndClearsError() = runTest(mainDispatcherRule.testDispatcher) {
        val api = FakeNotificationApi(
            initialNotifications = mutableListOf(
                NotificationReadDto(
                    id = 1,
                    title = "Existing",
                    message = "Old",
                    type = "course"
                )
            )
        )
        val viewModel = NotificationViewModel(NotificationRepository(api))

        advanceUntilIdle()
        viewModel.createNotification("New", "Body")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            assertEquals(2, state.notifications.size)
            assertEquals("New", state.notifications.last().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createNotificationPreservesExistingNotificationsOnError() = runTest(mainDispatcherRule.testDispatcher) {
        val api = FakeNotificationApi(
            initialNotifications = mutableListOf(
                NotificationReadDto(
                    id = 1,
                    title = "Existing",
                    message = "Old",
                    type = "course"
                )
            ),
            failOnCreate = true
        )
        val viewModel = NotificationViewModel(NotificationRepository(api))

        advanceUntilIdle()
        viewModel.createNotification("New", "Body")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("publish failed", state.errorMessage)
            assertEquals(1, state.notifications.size)
            assertEquals("Existing", state.notifications.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeNotificationApi(
    private val initialNotifications: MutableList<NotificationReadDto>,
    private val failOnCreate: Boolean = false
) : NotificationApi {
    private var nextId = (initialNotifications.maxOfOrNull { it.id } ?: 0) + 1

    override suspend fun listNotifications(): List<NotificationReadDto> {
        return initialNotifications.toList()
    }

    override suspend fun createNotification(request: NotificationCreateDto): NotificationReadDto {
        if (failOnCreate) {
            throw IllegalStateException("publish failed")
        }
        return NotificationReadDto(
            id = nextId++,
            title = request.title,
            message = request.message,
            type = request.type
        ).also(initialNotifications::add)
    }
}
