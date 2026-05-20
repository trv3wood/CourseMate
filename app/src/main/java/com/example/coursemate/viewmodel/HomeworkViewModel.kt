package com.example.coursemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemate.model.Homework
import com.example.coursemate.model.HomeworkSubmission
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
    val mySubmissions: List<HomeworkSubmission> = emptyList(),
    val submissionsByHomeworkId: Map<Int, List<HomeworkSubmission>> = emptyMap(),
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
    private val mySubmissions = MutableStateFlow<List<HomeworkSubmission>>(emptyList())
    private val submissionsByHomeworkId =
        MutableStateFlow<Map<Int, List<HomeworkSubmission>>>(emptyMap())

    val uiState: StateFlow<HomeworkUiState> = combine(
        cachedHomework,
        mySubmissions,
        submissionsByHomeworkId,
        operationState
    ) { homework, submissions, submissionGroups, operation ->
        HomeworkUiState(
            homework = homework,
            mySubmissions = submissions,
            submissionsByHomeworkId = submissionGroups,
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
            val homeworkResult = homeworkRepository.refreshHomework()
            val submissionResult = homeworkRepository.listMySubmissions()
            operationState.value = when {
                homeworkResult is AppResult.Error -> OperationUiState(errorMessage = homeworkResult.message)
                submissionResult is AppResult.Error -> OperationUiState(errorMessage = submissionResult.message)
                else -> {
                    mySubmissions.value = (submissionResult as AppResult.Success).data
                    OperationUiState()
                }
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

    fun updateHomework(
        homeworkId: Int,
        title: String? = null,
        content: String? = null,
        deadline: String? = null,
        status: String? = null
    ) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (
                val result = homeworkRepository.updateHomework(
                    homeworkId = homeworkId,
                    title = title,
                    content = content,
                    deadline = deadline,
                    status = status
                )
            ) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun deleteHomework(homeworkId: Int) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = homeworkRepository.deleteHomework(homeworkId)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
            submissionsByHomeworkId.value = submissionsByHomeworkId.value - homeworkId
        }
    }

    fun submitHomework(
        homeworkId: Int,
        content: String,
        attachmentUrl: String?,
        refreshSubmissionList: Boolean = false
    ) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (
                val result = homeworkRepository.submitHomework(homeworkId, content, attachmentUrl)
            ) {
                is AppResult.Success -> {
                    refreshMySubmissionsInternal()
                    if (refreshSubmissionList) {
                        loadHomeworkSubmissions(homeworkId)
                    }
                    OperationUiState()
                }
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun loadHomeworkSubmissions(homeworkId: Int) {
        viewModelScope.launch {
            when (val result = homeworkRepository.listHomeworkSubmissions(homeworkId)) {
                is AppResult.Success -> {
                    submissionsByHomeworkId.value = submissionsByHomeworkId.value + (
                        homeworkId to result.data
                    )
                }
                is AppResult.Error -> {
                    operationState.value = OperationUiState(errorMessage = result.message)
                }
            }
        }
    }

    private suspend fun refreshMySubmissionsInternal() {
        when (val result = homeworkRepository.listMySubmissions()) {
            is AppResult.Success -> mySubmissions.value = result.data
            is AppResult.Error -> operationState.value = OperationUiState(errorMessage = result.message)
        }
    }
}
