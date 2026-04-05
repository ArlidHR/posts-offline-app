package com.github.arlidhr.posts_offline_app.modules.comments.data.datasource

import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity

/**
 * Abstraction over remote data operations for Comments.
 * Decouples the repository from Retrofit for testability and future migration.
 */
interface CommentRemoteDataSource {

    /** Fetches all comments for a specific post from the API. */
    suspend fun getCommentsByPostId(postId: Int): List<CommentEntity>
}
