package com.example.coursemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemate.model.Course
import com.example.coursemate.repository.CourseRepository
import com.example.coursemate.utils.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CourseViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {
    private val operationState = MutableStateFlow(OperationUiState())
    private val cachedCourses = courseRepository.courses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<CourseUiState> = combine(
        cachedCourses,
        operationState
    ) { courses, operation ->
        CourseUiState(
            courses = courses,
            isLoading = operation.isLoading,
            errorMessage = operation.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CourseUiState()
    )

    init {
        refreshCourses()
    }

    fun refreshCourses() {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = courseRepository.refreshCourses()) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun createCourse(name: String, description: String?, teacherName: String) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (
                val result = courseRepository.createCourse(name, description, teacherName)
            ) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun updateCourse(
        courseId: Int,
        name: String? = null,
        description: String? = null,
        teacherName: String? = null
    ) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (
                val result = courseRepository.updateCourse(
                    courseId = courseId,
                    name = name,
                    description = description,
                    teacherName = teacherName
                )
            ) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = courseRepository.deleteCourse(courseId)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }
}
