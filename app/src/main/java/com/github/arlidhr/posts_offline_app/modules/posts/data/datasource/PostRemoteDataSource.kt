package com.github.arlidhr.posts_offline_app.modules.posts.data.datasource

import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity

/**
 * Abstraction over remote data operations for Posts.
 *
 * Decouples the repository from Retrofit's API service implementation,
 * enabling easy mocking in unit tests and future migration
 * to alternative HTTP clients (e.g., Ktor for KMP).
 */
interface PostRemoteDataSource {

    /** Fetches all posts from the remote API. */
    suspend fun getPosts(): List<PostEntity>

    /** Fetches a single post by its ID from the remote API. */
    suspend fun getPostById(id: Int): PostEntity
}
