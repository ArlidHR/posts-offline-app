package com.github.arlidhr.posts_offline_app.modules.comments.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the `comments` table.
 *
 * Supports both API-fetched and user-created local comments.
 * Methods returning [Flow] enable reactive UI updates when comments change.
 */
@Dao
interface CommentDao {

    /**
     * Observes all comments for a specific post, ordered by ID.
     * Returns both API-fetched and local user-created comments.
     */
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY id ASC")
    fun getCommentsByPostId(postId: Int): Flow<List<CommentEntity>>

    /**
     * Inserts a list of comments (typically from the API).
     * Replaces on conflict to handle re-fetches without duplicates.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<CommentEntity>)

    /**
     * Inserts a single local user-created comment.
     * Uses REPLACE strategy though local comments should have auto-generated unique IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    /**
     * Deletes all API-fetched comments for a post (preserves local user comments).
     * Used before re-fetching from API to avoid stale data.
     */
    @Query("DELETE FROM comments WHERE postId = :postId AND isLocal = 0")
    suspend fun deleteRemoteCommentsByPostId(postId: Int)
}
