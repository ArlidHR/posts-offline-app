package com.github.arlidhr.posts_offline_app.modules.posts.domain.repository

import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.core.error.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for Posts — defined in the Domain layer.
 *
 * The implementation lives in the Data layer ([PostRepositoryImpl]),
 * following the Dependency Inversion Principle: Domain defines the contract,
 * Data provides the implementation.
 *
 * All methods return [Result] to communicate loading, success, and error states
 * in a consistent way across the application.
 */
interface PostRepository {

    /**
     * Observes all posts with offline-first strategy.
     * Emits cached data first, then refreshes from API in the background.
     */
    fun getPosts(): Flow<Result<List<Post>>>

    /**
     * Searches posts by title or ID with offline-first strategy.
     * Search is performed against the local database.
     */
    fun searchPosts(query: String): Flow<Result<List<Post>>>

    /**
     * Retrieves a single post by ID from the local database.
     */
    suspend fun getPostById(id: Int): Result<Post>

    /**
     * Forces a refresh of posts from the remote API.
     * Downloads all posts and replaces the local cache.
     */
    suspend fun refreshPosts(): Result<Unit>
}
