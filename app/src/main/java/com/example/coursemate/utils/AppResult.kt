package com.example.coursemate.utils

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}

suspend inline fun <T> safeCall(crossinline block: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (throwable: Throwable) {
        AppResult.Error(
            message = throwable.message ?: throwable::class.java.simpleName,
            cause = throwable
        )
    }
}
