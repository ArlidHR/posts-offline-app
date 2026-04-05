package com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * Use case: Add a new local comment to a post.
 *
 * Validates that the required fields are not blank before persisting.
 * The comment is stored only in Room — never sent to the API.
 *
 * Fulfills functional requirement:
 * "En la pantalla donde se listan los comentarios debo tener la opción
 *  de crear N comentarios a dicha publicación."
 */
class AddCommentUseCase @Inject constructor(
    private val repository: CommentRepository
) {
    suspend operator fun invoke(postId: Int, name: String, body: String): Result<Unit> {
        if (name.isBlank() || body.isBlank()) {
            return Result.Error("Name and body cannot be empty")
        }
        val comment = Comment(
            postId = postId,
            name = name.trim(),
            body = body.trim(),
            isLocal = true
        )
        return repository.addLocalComment(comment)
    }
}
