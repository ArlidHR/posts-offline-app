package com.github.arlidhr.posts_offline_app.modules.posts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the `posts` table.
 *
 * Methods returning [Flow] are reactive — Room automatically re-emits
 * whenever the underlying data changes (e.g., after [upsertAll]).
 * This is the backbone of the offline-first strategy.
 */
@Dao
interface PostDao {

    /**
     * Observes all posts ordered by ID.
     * Returns a [Flow] that emits a new list whenever the table changes.
     */
    @Query("SELECT * FROM posts ORDER BY id ASC")
    fun getAllPosts(): Flow<List<PostEntity>>

    /**
     * Retrieves a single post by its ID. Returns null if not found.
     */
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Int): PostEntity?

    /**
     * Searches posts by title or ID.
     * The query matches partial text in the title (case-insensitive via LIKE)
     * or an exact/partial match on the ID cast as text.
     *
     * Returns a [Flow] for reactive updates during search.
     */
    @Query(
        """
        SELECT * FROM posts 
        WHERE title LIKE '%' || :query || '%' 
           OR CAST(id AS TEXT) LIKE '%' || :query || '%'
        ORDER BY id ASC
        """
    )
    fun searchPosts(query: String): Flow<List<PostEntity>>

    /**
     * Upserts a list of posts (INSERT or UPDATE on conflict).
     *
     * CRITICAL: Uses @Upsert instead of @Insert(onConflict = REPLACE).
     * REPLACE internally does DELETE + INSERT, which triggers ForeignKey CASCADE
     * and destroys all child comments (including user-created local ones).
     * @Upsert does INSERT OR IGNORE + UPDATE, which does NOT trigger CASCADE.
     */
    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    /**
     * Deletes only posts whose IDs are NOT in the given list.
     * Used during refresh to remove stale posts without triggering
     * CASCADE deletes on posts that still exist (preserving their local comments).
     */
    @Query("DELETE FROM posts WHERE id NOT IN (:ids)")
    suspend fun deletePostsNotIn(ids: List<Int>)

    /**
     * Atomically refreshes posts from the API using a smart-upsert strategy:
     * 1. Delete only posts that no longer exist on the server (stale cleanup).
     * 2. Upsert all current posts (INSERT OR REPLACE by PrimaryKey).
     *
     * This avoids deleting posts that still exist, so their CASCADE foreign key
     * does NOT trigger on the comments table — preserving user-created local comments.
     *
     * Previous implementation used deleteAll() + insertAll(), which triggered
     * CASCADE deletes on ALL comments (including isLocal = true) every refresh.
     */
    @Transaction
    suspend fun refreshPosts(posts: List<PostEntity>) {
        val freshIds = posts.map { it.id }
        deletePostsNotIn(freshIds)
        upsertAll(posts)
    }
}
