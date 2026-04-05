package com.github.arlidhr.posts_offline_app.modules.comments.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.core.navigation.Routes
import com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase.AddCommentUseCase
import com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase.GetCommentsUseCase
import com.github.arlidhr.posts_offline_app.modules.comments.presentation.state.CommentsUiState
import com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase.GetPostByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Comments screen.
 *
 * Receives [postId] via [SavedStateHandle] (injected by Navigation + Hilt automatically).
 * Manages loading comments, displaying the post title, and the add-comment form.
 *
 * @param savedStateHandle Provides the postId argument from the navigation back stack.
 */
@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostByIdUseCase: GetPostByIdUseCase
) : ViewModel() {

    private val postId: Int = checkNotNull(savedStateHandle[Routes.PostComments.ARG_POST_ID])

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    init {
        loadPostTitle()
        loadComments()
    }

    /**
     * Loads the post title to display in the TopAppBar.
     */
    private fun loadPostTitle() {
        viewModelScope.launch {
            when (val result = getPostByIdUseCase(postId)) {
                is Result.Success -> _uiState.update { it.copy(postTitle = result.data.title) }
                else -> Unit // Title stays empty — non-critical
            }
        }
    }

    /**
     * Loads all comments for this post (offline-first).
     */
    fun loadComments() {
        viewModelScope.launch {
            getCommentsUseCase(postId).collect { result ->
                _uiState.update { currentState ->
                    when (result) {
                        is Result.Loading -> currentState.copy(isLoading = true, error = null)
                        is Result.Success -> currentState.copy(
                            comments = result.data,
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

    fun onNewCommentNameChange(value: String) {
        _uiState.update { it.copy(newCommentName = value, addCommentError = null) }
    }

    fun onNewCommentBodyChange(value: String) {
        _uiState.update { it.copy(newCommentBody = value, addCommentError = null) }
    }

    /**
     * Saves the new local comment.
     * Clears the form on success; sets [CommentsUiState.addCommentError] on failure.
     */
    fun submitComment() {
        val name = _uiState.value.newCommentName
        val body = _uiState.value.newCommentBody

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingComment = true, addCommentError = null) }

            when (val result = addCommentUseCase(postId, name, body)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isAddingComment = false,
                            newCommentName = "",
                            newCommentBody = ""
                        )
                    }
                    // Room Flow re-emits automatically — no manual refresh needed
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isAddingComment = false,
                            addCommentError = result.message
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }
}
