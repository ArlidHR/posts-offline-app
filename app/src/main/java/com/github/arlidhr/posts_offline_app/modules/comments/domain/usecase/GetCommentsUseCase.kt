package com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Retrieve all comments for a specific post.
 * Offline-first — returns cached data immediately, refreshes from API when connected.
 */
class GetCommentsUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    operator fun invoke(postId: Int): Flow<Result<List<Comment>>> =
        repository.getCommentsByPostId(postId)
}
