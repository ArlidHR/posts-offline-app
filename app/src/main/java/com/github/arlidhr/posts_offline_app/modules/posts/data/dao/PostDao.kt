package com.github.arlidhr.posts_offline_app.modules.posts.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the `posts` table.
 *
 * Methods returning [Flow] are reactive — Room automatically re-emits
 * whenever the underlying data changes (e.g., after [insertAll]).
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
     * Inserts a list of posts. Replaces existing entries on conflict (same ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<PostEntity>)

    /**
     * Deletes all posts from the table.
     */
    @Query("DELETE FROM posts")
    suspend fun deleteAll()

    /**
     * Atomically replaces all posts: deletes existing data and inserts fresh data.
     * Wrapped in a [Transaction] to ensure data consistency — if insertion fails,
     * the deletion is rolled back.
     */
    @Transaction
    suspend fun refreshPosts(posts: List<PostEntity>) {
        deleteAll()
        insertAll(posts)
    }
}
