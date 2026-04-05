package com.github.arlidhr.posts_offline_app.modules.posts.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.arlidhr.posts_offline_app.components.AppSearchBar
import com.github.arlidhr.posts_offline_app.components.EmptyState
import com.github.arlidhr.posts_offline_app.components.ErrorMessage
import com.github.arlidhr.posts_offline_app.components.LoadingIndicator
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.presentation.state.PostsUiState
import com.github.arlidhr.posts_offline_app.modules.posts.presentation.viewmodel.PostsViewModel

/**
 * Main screen displaying the list of posts.
 *
 * Features:
 * - Search bar for filtering by title or ID
 * - Pull-to-refresh for manual data refresh
 * - Loading, error, and empty state handling via reusable components
 * - Click on a post navigates to its comments
 *
 * Uses [hiltViewModel] for automatic ViewModel injection and
 * [collectAsStateWithLifecycle] for lifecycle-aware state collection.
 *
 * @param onPostClick Callback when a post is tapped, receives the post ID.
 * @param viewModel Injected automatically by Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsListScreen(
    onPostClick: (postId: Int) -> Unit,
    viewModel: PostsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Posts") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                placeholder = "Search by title or ID...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content area with pull-to-refresh
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = viewModel::loadPosts,
                modifier = Modifier.fillMaxSize()
            ) {
                PostsContent(
                    uiState = uiState,
                    onPostClick = onPostClick,
                    onRetry = viewModel::loadPosts
                )
            }
        }
    }
}

/**
 * Content area that renders the appropriate state:
 * loading indicator, error message, empty state, or posts list.
 */
@Composable
private fun PostsContent(
    uiState: PostsUiState,
    onPostClick: (postId: Int) -> Unit,
    onRetry: () -> Unit
) {
    when {
        // Initial loading — no data yet
        uiState.isLoading && uiState.posts.isEmpty() -> {
            LoadingIndicator(message = "Loading posts...")
        }

        // Error with no cached data
        uiState.error != null && uiState.posts.isEmpty() -> {
            ErrorMessage(
                message = uiState.error,
                onRetry = onRetry
            )
        }

        // Empty state (no results)
        uiState.isEmpty -> {
            val message = if (uiState.isSearchActive) {
                "No posts match your search."
            } else {
                "No posts available.\nPull down to refresh."
            }
            EmptyState(message = message)
        }

        // Posts list — may be showing while refreshing in background
        else -> {
            PostsList(
                posts = uiState.posts,
                onPostClick = onPostClick
            )
        }
    }
}

/**
 * Scrollable list of post cards.
 */
@Composable
private fun PostsList(
    posts: List<Post>,
    onPostClick: (postId: Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = posts,
            key = { it.id }
        ) { post ->
            PostItem(
                post = post,
                onClick = { onPostClick(post.id) }
            )
        }
    }
}
