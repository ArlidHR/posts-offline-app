package com.github.arlidhr.posts_offline_app.modules.comments.domain.repository

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for Comments — defined in the Domain layer.
 *
 * Supports two types of comments:
 * - **API comments**: fetched from the network, cached in Room.
 * - **Local comments**: created by the user, stored only in Room, never synced.
 */
interface CommentRepository {

    /**
     * Observes all comments for a post with offline-first strategy.
     * Returns both API-fetched and local user-created comments.
     */
    fun getCommentsByPostId(postId: Int): Flow<Result<List<Comment>>>

    /**
     * Adds a new local user-created comment to a post.
     * Stored only in Room — never sent to the API.
     */
    suspend fun addLocalComment(comment: Comment): Result<Unit>
}
