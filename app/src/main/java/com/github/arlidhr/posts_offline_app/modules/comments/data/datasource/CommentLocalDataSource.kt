package com.github.arlidhr.posts_offline_app.modules.comments.data.datasource

import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over local data operations for Comments.
 * Decouples the repository from Room's DAO for testability and future migration.
 */
interface CommentLocalDataSource {

    /** Observes all comments for a specific post. */
    fun getCommentsByPostId(postId: Int): Flow<List<CommentEntity>>

    /** Inserts a list of API-fetched comments. */
    suspend fun insertAll(comments: List<CommentEntity>)

    /** Inserts a single user-created local comment. */
    suspend fun insert(comment: CommentEntity)

    /** Deletes only API-fetched comments for a post (preserves local). */
    suspend fun deleteRemoteCommentsByPostId(postId: Int)
}
