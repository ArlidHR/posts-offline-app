package com.github.arlidhr.posts_offline_app.core.error

/**
 * Generic sealed class to wrap API/DB responses.
 * Used across all modules for consistent error handling.
 *
 * - Result.Success<T>(data)
 * - Result.Error(message, throwable)
 * - Result.Loading
 */

