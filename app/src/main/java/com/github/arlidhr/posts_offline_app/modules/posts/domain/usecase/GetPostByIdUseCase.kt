package com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Use case: Retrieve a single post by its ID.
 *
 * Used when navigating to the comments screen to display
 * the post title/body alongside its comments.
 *
 * Queries the local database only — the post should already be cached
 * from the initial list fetch.
 */
class GetPostByIdUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: Int): Result<Post> =
        repository.getPostById(postId)
}
