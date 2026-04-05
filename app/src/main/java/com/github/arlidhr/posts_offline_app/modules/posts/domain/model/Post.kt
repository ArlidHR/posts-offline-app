package com.github.arlidhr.posts_offline_app.modules.posts.domain.model

/**
 * Domain model representing a Post.
 *
 * Pure Kotlin data class with no framework dependencies (no Room, no Gson annotations).
 * Used across Domain and Presentation layers as the single source of truth for post data.
 *
 * @property id Unique identifier of the post.
 * @property userId Identifier of the user who created the post.
 * @property title Title of the post.
 * @property body Content body of the post.
 */
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)
