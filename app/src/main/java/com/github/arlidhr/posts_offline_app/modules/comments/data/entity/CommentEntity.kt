package com.github.arlidhr.posts_offline_app.modules.comments.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity

/**
 * Room entity representing a Comment in the local database.
 *
 * Features:
 * - [ForeignKey] to [PostEntity] ensures referential integrity (comment → post).
 * - [Index] on postId for fast lookups when loading comments for a post.
 * - [isLocal] flag distinguishes user-created comments from API-fetched ones.
 * - [PrimaryKey] auto-generates IDs for local comments, avoiding collision with API IDs.
 *
 * API comments use their original ID. Local comments get auto-generated IDs
 * starting from a high range (autoGenerate = true uses SQLite ROWID).
 */
@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["postId"])]
)
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "postId")
    val postId: Int,
    val name: String,
    val email: String,
    val body: String,
    val isLocal: Boolean = false
) {
    fun toDomain(): Comment = Comment(
        id = id,
        postId = postId,
        name = name,
        email = email,
        body = body,
        isLocal = isLocal
    )
}

fun Comment.toEntity(): CommentEntity = CommentEntity(
    id = id,
    postId = postId,
    name = name,
    email = email,
    body = body,
    isLocal = isLocal
)
