package com.github.arlidhr.posts_offline_app.modules.posts.data.service

import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API Service interface for Posts endpoints.
 * GET /posts, GET /posts/{id}
 *
 * Returns [PostEntity] directly since the JSON field names match
 * the entity properties — no intermediate DTO layer needed for this scope.
 *
 * Base URL is configured in [NetworkModule]: https://jsonplaceholder.typicode.com/
 */
interface PostApiService {

    /**
     * Fetches all posts from the API.
     * Endpoint: GET /posts
     */
    @GET("posts")
    suspend fun getPosts(): List<PostEntity>

    /**
     * Fetches a single post by its ID.
     * Endpoint: GET /posts/{id}
     */
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostEntity
}
