package com.github.arlidhr.posts_offline_app.modules.comments.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arlidhr.posts_offline_app.components.EmptyState
import com.github.arlidhr.posts_offline_app.components.ErrorMessage
import com.github.arlidhr.posts_offline_app.components.LoadingIndicator
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.presentation.state.CommentsUiState
import com.github.arlidhr.posts_offline_app.modules.comments.presentation.viewmodel.CommentsViewModel

/**
 * Screen displaying comments for a specific post with the ability to add new local comments.
 *
 * Layout (top to bottom):
 * 1. TopAppBar — post title + back button
 * 2. Comments list (pull-to-refresh) — API + local comments
 * 3. Sticky add-comment form — two text fields + submit button
 *
 * The form stays at the bottom and the list scrolls above it,
 * keeping the UX natural for adding comments.
 *
 * @param onNavigateBack Callback to pop the back stack.
 * @param viewModel Injected automatically by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CommentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.postTitle.ifBlank { "Comments" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Comments list with pull-to-refresh
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = viewModel::loadComments,
                modifier = Modifier.weight(1f)
            ) {
                CommentsContent(
                    uiState = uiState,
                    onRetry = viewModel::loadComments
                )
            }

            HorizontalDivider()

            // Add comment form — always visible at the bottom
            AddCommentForm(
                name = uiState.newCommentName,
                body = uiState.newCommentBody,
                isSubmitting = uiState.isAddingComment,
                canSubmit = uiState.canSubmitComment,
                error = uiState.addCommentError,
                onNameChange = viewModel::onNewCommentNameChange,
                onBodyChange = viewModel::onNewCommentBodyChange,
                onSubmit = viewModel::submitComment
            )
        }
    }
}

/**
 * Content area for the comments list: handles loading, error, empty, and list states.
 */
@Composable
private fun CommentsContent(
    uiState: CommentsUiState,
    onRetry: () -> Unit
) {
    when {
        uiState.isLoading && uiState.comments.isEmpty() -> {
            LoadingIndicator(message = "Loading comments...")
        }
        uiState.error != null && uiState.comments.isEmpty() -> {
            ErrorMessage(message = uiState.error, onRetry = onRetry)
        }
        uiState.isEmpty -> {
            EmptyState(message = "No comments yet.\nBe the first to comment!")
        }
        else -> {
            CommentsList(comments = uiState.comments)
        }
    }
}

/**
 * Scrollable list of comment cards.
 */
@Composable
private fun CommentsList(comments: List<Comment>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = comments,
            key = { "${it.id}_${it.isLocal}" }
        ) { comment ->
            CommentItem(comment = comment)
        }
    }
}

/**
 * Sticky form at the bottom for adding a new local comment.
 *
 * Both fields are required. The submit button is disabled until both are non-blank.
 * Shows a progress indicator while saving and surfaces validation errors.
 */
@Composable
private fun AddCommentForm(
    name: String,
    body: String,
    isSubmitting: Boolean,
    canSubmit: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Add a comment",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name / Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null && name.isBlank()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = body,
            onValueChange = onBodyChange,
            label = { Text("Comment") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            isError = error != null && body.isBlank()
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    onClick = onSubmit,
                    enabled = canSubmit
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 4.dp)
                    )
                    Text("Post")
                }
            }
        }
    }
}
