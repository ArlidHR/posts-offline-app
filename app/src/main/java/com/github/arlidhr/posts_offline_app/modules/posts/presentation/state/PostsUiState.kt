package com.github.arlidhr.posts_offline_app.modules.posts.presentation.state

import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post

/**
 * Immutable UI state for the Posts list screen.
 *
 * Represents every possible state the screen can be in. The ViewModel produces
 * this state, and the Composable renders it — unidirectional data flow.
 *
 * @property posts The current list of posts to display.
 * @property isLoading Whether a loading operation is in progress.
 * @property error Error message to display, or null if no error.
 * @property searchQuery The current search query text from the search bar.
 * @property isSearchActive Whether the user is actively searching (filters results).
 */
data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false
) {
    /** True when there are no posts and no loading/error state — shows empty placeholder. */
    val isEmpty: Boolean
        get() = posts.isEmpty() && !isLoading && error == null
}
