package com.github.arlidhr.posts_offline_app.modules.comments.presentation.state

import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment

/**
 * Immutable UI state for the Comments screen.
 *
 * Represents every possible state: loading, error, list of comments,
 * and the "add comment" form state — all in a single object.
 *
 * @property comments All comments for the post (API + local).
 * @property isLoading Whether comments are being loaded.
 * @property error Error message, null if no error.
 * @property postTitle Title of the post shown in the TopAppBar.
 * @property newCommentName Text field value for the new comment's name/title.
 * @property newCommentBody Text field value for the new comment's body.
 * @property isAddingComment Whether a comment save operation is in progress.
 * @property addCommentError Validation/save error for the form, null if none.
 */
data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val postTitle: String = "",
    val newCommentName: String = "",
    val newCommentBody: String = "",
    val isAddingComment: Boolean = false,
    val addCommentError: String? = null
) {
    /** True when there are no comments and no loading/error — shows empty placeholder. */
    val isEmpty: Boolean
        get() = comments.isEmpty() && !isLoading && error == null

    /** True when both form fields have content — enables the submit button. */
    val canSubmitComment: Boolean
        get() = newCommentName.isNotBlank() && newCommentBody.isNotBlank()
}
