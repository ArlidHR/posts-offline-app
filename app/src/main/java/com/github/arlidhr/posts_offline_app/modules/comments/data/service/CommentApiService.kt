package com.github.arlidhr.posts_offline_app.modules.comments.data.service

import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit API Service interface for Comments endpoints.
 * GET /posts/{postId}/comments
 *
 * Note: The API is read-only for comments. User-created comments are stored
 * locally only and never sent to the API (as per requirements).
 */
interface CommentApiService {

    /**
     * Fetches all comments for a specific post.
     * Endpoint: GET /posts/{postId}/comments
     */
    @GET("posts/{postId}/comments")
    suspend fun getCommentsByPostId(@Path("postId") postId: Int): List<CommentEntity>
}
