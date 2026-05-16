package com.example.coursemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemate.model.User
import com.example.coursemate.repository.AuthRepository
import com.example.coursemate.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: User? = null,
    val token: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val operationState = MutableStateFlow(OperationUiState())

    val uiState: StateFlow<AuthUiState> = combine(
        authRepository.currentUser,
        authRepository.authToken,
        operationState
    ) { user, token, operation ->
        AuthUiState(
            currentUser = user,
            token = token,
            isLoading = operation.isLoading,
            errorMessage = operation.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthUiState()
    )

    fun login(username: String, password: String) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = authRepository.login(username, password)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun register(username: String, email: String, password: String, role: String = "student") {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = authRepository.register(username, email, password, role)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = authRepository.refreshCurrentUser()) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            operationState.value = OperationUiState()
        }
    }
}
