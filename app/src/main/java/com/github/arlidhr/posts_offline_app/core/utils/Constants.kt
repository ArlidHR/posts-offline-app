package com.github.arlidhr.posts_offline_app.core.utils

/**
 * App-wide constants.
 * Centralized to avoid magic strings scattered across the codebase.
 */
object Constants {
    const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    const val DATABASE_NAME = "posts_offline_db"
    const val CONNECT_TIMEOUT_SECONDS = 15L
    const val READ_TIMEOUT_SECONDS = 15L
}

