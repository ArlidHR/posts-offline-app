package com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Retrieve all posts with offline-first strategy.
 *
 * Encapsulates the single action of fetching the posts list.
 * Following the Single Responsibility Principle — one use case, one action.
 *
 * Uses `operator fun invoke()` for clean call-site syntax:
 * ```
 * val posts = getPostsUseCase()  // instead of getPostsUseCase.execute()
 * ```
 *
 * If business rules are added in the future (e.g., filtering, sorting,
 * combining with user data), this is the place to add them — not in
 * the ViewModel or Repository.
 */
class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(): Flow<Result<List<Post>>> =
        repository.getPosts()
}
