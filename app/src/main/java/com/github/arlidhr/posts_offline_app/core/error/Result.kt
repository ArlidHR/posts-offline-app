package com.github.arlidhr.posts_offline_app.core.error

/**
 * Generic sealed class to wrap API/DB responses.
 * Used across all modules for consistent error handling and UI state management.
 *
 * Usage:
 *   Result.Success(data)   → operation succeeded
 *   Result.Error(message)  → operation failed
 *   Result.Loading          → operation in progress
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if Success, or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}
