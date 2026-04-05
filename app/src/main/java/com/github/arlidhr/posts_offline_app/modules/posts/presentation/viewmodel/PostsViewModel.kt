package com.github.arlidhr.posts_offline_app.modules.posts.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase.GetPostsUseCase
import com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase.SearchPostsUseCase
import com.github.arlidhr.posts_offline_app.modules.posts.presentation.state.PostsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Posts list screen.
 *
 * Manages [PostsUiState] via [StateFlow] for reactive, lifecycle-aware UI updates.
 * Handles loading posts, searching with debounce, and pull-to-refresh.
 *
 * Data flow:
 * ```
 * User action → ViewModel → UseCase → Repository → Room/API
 *                  ↑                                    │
 *                  └──── StateFlow<PostsUiState> ←──────┘
 * ```
 */
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val searchPostsUseCase: SearchPostsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 300L
    }

    init {
        loadPosts()
    }

    /**
     * Loads all posts using the offline-first strategy.
     * Called on init and on pull-to-refresh.
     */
    fun loadPosts() {
        viewModelScope.launch {
            getPostsUseCase().collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> currentState.copy(
                            isLoading = true,
                            error = null
                        )
                        is Result.Success -> currentState.copy(
                            posts = result.data,
                            isLoading = false,
                            error = null
                        )
                        is Result.Error -> currentState.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates the search query and triggers a debounced search.
     *
     * Debounce prevents firing a database query on every keystroke.
     * If the query is blank, reverts to showing all posts.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)

            if (query.isBlank()) {
                _uiState.update { it.copy(isSearchActive = false) }
                loadPosts()
                return@launch
            }

            _uiState.update { it.copy(isSearchActive = true) }

            searchPostsUseCase(query).collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> currentState.copy(isLoading = true)
                        is Result.Success -> currentState.copy(
                            posts = result.data,
                            isLoading = false,
                            error = null
                        )
                        is Result.Error -> currentState.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    /**
     * Clears the search and reloads all posts.
     */
    fun clearSearch() {
        onSearchQueryChange("")
    }
}
