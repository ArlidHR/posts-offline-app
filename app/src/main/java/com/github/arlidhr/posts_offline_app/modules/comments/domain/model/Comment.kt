package com.github.arlidhr.posts_offline_app.modules.comments.domain.model

/**
 * Domain model representing a Comment.
 *
 * Pure Kotlin data class with no framework dependencies.
 * Used across Domain and Presentation layers.
 *
 * @property id Unique identifier. Auto-generated for local comments.
 * @property postId ID of the post this comment belongs to.
 * @property name Author name or comment title.
 * @property email Author email. Empty for user-created local comments.
 * @property body Content of the comment.
 * @property isLocal True if created locally by the user (not synced to API).
 */
data class Comment(
    val id: Int = 0,
    val postId: Int,
    val name: String,
    val email: String = "",
    val body: String,
    val isLocal: Boolean = false
)
