package com.github.arlidhr.posts_offline_app.modules.posts.data.datasource

import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over local data operations for Posts.
 *
 * Decouples the repository from Room's DAO implementation,
 * enabling easy mocking in unit tests and future migration
 * to alternative persistence solutions (e.g., SQLDelight for KMP).
 */
interface PostLocalDataSource {

    /** Observes all posts from the local database. */
    fun getAllPosts(): Flow<List<PostEntity>>

    /** Retrieves a single post by ID. Returns null if not found. */
    suspend fun getPostById(id: Int): PostEntity?

    /** Searches posts by title or ID. */
    fun searchPosts(query: String): Flow<List<PostEntity>>

    /** Atomically replaces all cached posts with fresh data. */
    suspend fun refreshPosts(posts: List<PostEntity>)
}
