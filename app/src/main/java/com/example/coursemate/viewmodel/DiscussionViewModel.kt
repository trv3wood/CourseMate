package com.example.coursemate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursemate.model.Post
import com.example.coursemate.model.PostDetail
import com.example.coursemate.repository.PostRepository
import com.example.coursemate.utils.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiscussionUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class DiscussionViewModel(
    private val postRepository: PostRepository
) : ViewModel() {
    private val operationState = MutableStateFlow(OperationUiState())
    private val cachedPosts = postRepository.posts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<DiscussionUiState> = combine(
        cachedPosts,
        operationState
    ) { posts, operation ->
        DiscussionUiState(
            posts = posts,
            isLoading = operation.isLoading,
            errorMessage = operation.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscussionUiState()
    )

    init {
        refreshPosts()
    }

    fun observePostDetail(postId: Int): Flow<PostDetail?> {
        return postRepository.observePostDetail(postId)
    }

    fun refreshPosts() {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = postRepository.refreshPosts()) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun refreshPostDetail(postId: Int) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = postRepository.refreshPostDetail(postId)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun createPost(courseId: Int, title: String, content: String) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = postRepository.createPost(courseId, title, content)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun replyToPost(postId: Int, content: String) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = postRepository.replyToPost(postId, content)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }

    fun acceptReply(replyId: Int) {
        viewModelScope.launch {
            operationState.value = OperationUiState(isLoading = true)
            operationState.value = when (val result = postRepository.acceptReply(replyId)) {
                is AppResult.Success -> OperationUiState()
                is AppResult.Error -> OperationUiState(errorMessage = result.message)
            }
        }
    }
}
