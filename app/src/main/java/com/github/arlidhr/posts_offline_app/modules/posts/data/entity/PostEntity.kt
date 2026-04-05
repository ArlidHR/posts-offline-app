package com.github.arlidhr.posts_offline_app.modules.posts.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post

/**
 * Room Entity representing a Post in the local database.
 * Maps directly to the 'posts' table.
 */
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    /**
     * Maps this data entity to a domain [Post] model.
     */
    fun toDomain(): Post = Post(
        id = id,
        userId = userId,
        title = title,
        body = body
    )
}

/**
 * Extension function to map a domain [Post] to a [PostEntity].
 */
fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    title = title,
    body = body
)
