package com.example.coursemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemate.model.Notification
import com.example.coursemate.repository.NotificationRepository
import com.example.coursemate.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        refreshNotifications()
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = when (val result = notificationRepository.listNotifications()) {
                is AppResult.Success -> NotificationUiState(notifications = result.data)
                is AppResult.Error -> NotificationUiState(errorMessage = result.message)
            }
        }
    }

    fun createNotification(
        title: String,
        message: String,
        type: String = "course",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val existing = _uiState.value.notifications
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = notificationRepository.createNotification(title, message, type)) {
                is AppResult.Success -> {
                    onSuccess()
                    refreshNotifications()
                }
                is AppResult.Error -> {
                    _uiState.value = NotificationUiState(
                        notifications = existing,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
}
