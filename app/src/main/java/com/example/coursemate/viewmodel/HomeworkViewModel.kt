package com.example.coursemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemate.model.Homework
import com.example.coursemate.repository.HomeworkRepository
import com.example.coursemate.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeworkUiState(
    val homework: List<Homework> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeworkViewModel(
    private val homeworkRepository: HomeworkRepository
) : ViewModel() {
    private val operationState = MutableStateFlow(OperationUiState())
    private val cachedHomework = homeworkRepository.homework.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<HomeworkUiState> = combine(
        cachedHomework,
        operationState
    ) { homework, operation ->
        HomeworkUiState(
            homework = homework,
            isLoading = operation.isLoading,
            errorMessage = operation.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeworkUiState()
    )

    init {
        refreshHomework()
    }

    fun refreshHomework() {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = homeworkRepository.refreshHomework()) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun createHomework(
        courseId: Int,
        title: String,
        content: String,
        deadline: String?,
        status: String = "open"
    ) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (
                val result = homeworkRepository.createHomework(courseId, title, content, deadline, status)
            ) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }
}
