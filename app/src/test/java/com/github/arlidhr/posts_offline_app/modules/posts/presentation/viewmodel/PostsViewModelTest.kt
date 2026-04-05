package com.github.arlidhr.posts_offline_app.modules.posts.presentation.viewmodel

import app.cash.turbine.test
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase.GetPostsUseCase
import com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase.SearchPostsUseCase
import com.github.arlidhr.posts_offline_app.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [PostsViewModel].
 *
 * Covers:
 * - Initial load on creation (init block → loadPosts).
 * - State transitions: Loading → Success / Error.
 * - Search debounce: only the final query fires after 300 ms.
 * - Debounce cancellation on rapid key presses.
 * - clearSearch() restores all-posts mode.
 *
 * Uses [MainDispatcherRule] so that `viewModelScope` runs on a [kotlinx.coroutines.test.UnconfinedTestDispatcher].
 * Debounce tests use `runTest(mainDispatcherRule.testDispatcher)` to share the virtual clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getPostsUseCase: GetPostsUseCase = mockk()
    private val searchPostsUseCase: SearchPostsUseCase = mockk()

    @Before
    fun setUp() {
        // Default stubs: both use cases must be configured so that:
        // - init { loadPosts() } succeeds on ViewModel creation
        // - runTest auto-drain at the end of each test doesn't throw
        //   when the debounce coroutine eventually fires
        every { getPostsUseCase() } returns flowOf(Result.Success(emptyList()))
        every { searchPostsUseCase(any()) } returns flowOf(Result.Success(emptyList()))
    }

    // ─── Initial load ──────────────────────────────────────────────────────

    @Test
    fun `init triggers loadPosts immediately on creation`() = runTest {
        createViewModel()

        @Suppress("UNUSED_EXPRESSION")
        verify(exactly = 1) { getPostsUseCase() }
    }

    @Test
    fun `loadPosts updates state to success with list of posts`() = runTest {
        // Given
        val posts = listOf(fakePost(id = 1), fakePost(id = 2))
        every { getPostsUseCase() } returns flowOf(Result.Loading, Result.Success(posts))

        // When — with UnconfinedTestDispatcher the flow is consumed before the next line
        val viewModel = createViewModel()

        // Then
        assertEquals(posts, viewModel.uiState.value.posts)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadPosts sets error message when repository emits error`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.Error("Network error"))

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals("Network error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.posts.isEmpty())
    }

    @Test
    fun `loadPosts sets isLoading true while flow is pending`() = runTest {
        // Given — flow emits Loading and never completes (simulates in-flight network call)
        every { getPostsUseCase() } returns flow { emit(Result.Loading) }

        // When
        val viewModel = createViewModel()

        // Then — state is captured with Turbine to get the most recent emission
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isEmpty is true when posts are empty with no loading or error`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.Success(emptyList()))

        // When
        val viewModel = createViewModel()

        // Then
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    // ─── Search with debounce ──────────────────────────────────────────────
    //
    // Root cause note:
    // With UnconfinedTestDispatcher, `advanceTimeBy` advances the virtual clock
    // but does NOT guarantee running the woken-up coroutines immediately.
    // `advanceUntilIdle()` must be used: it advances time to the next scheduled
    // task AND runs it, repeating until the scheduler is truly idle.
    //
    // Scheduler sharing: `runTest(testDispatcher.scheduler)` ensures that the
    // TestScope and viewModelScope share the same TestCoroutineScheduler, so
    // `advanceUntilIdle()` here affects the ViewModel's debounce delay.

    @Test
    fun `onSearchQueryChange updates searchQuery immediately before debounce fires`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = createViewModel()

            viewModel.onSearchQueryChange("kotlin")

            // Query is updated synchronously before the debounce fires
            assertEquals("kotlin", viewModel.uiState.value.searchQuery)
        }

    @Test
    fun `onSearchQueryChange with valid query triggers search after debounce`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            // Given
            val results = listOf(fakePost(title = "Kotlin Coroutines"))
            every { searchPostsUseCase("kotlin") } returns flowOf(Result.Success(results))

            val viewModel = createViewModel()

            // When
            viewModel.onSearchQueryChange("kotlin")
            advanceUntilIdle() // advance past SEARCH_DEBOUNCE_MS and run the coroutine

            // Then
            @Suppress("UNUSED_EXPRESSION")
            verify(exactly = 1) { searchPostsUseCase("kotlin") }
            assertEquals(results, viewModel.uiState.value.posts)
            assertTrue(viewModel.uiState.value.isSearchActive)
        }

    @Test
    fun `onSearchQueryChange cancels previous search job on rapid keystrokes`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = createViewModel()

            // When — rapid keystrokes: each call cancels the previous debounce job
            viewModel.onSearchQueryChange("k")
            viewModel.onSearchQueryChange("ko")
            viewModel.onSearchQueryChange("kot")
            advanceUntilIdle() // only "kot" debounce survives the cancellations

            // Then
            @Suppress("UNUSED_EXPRESSION")
            verify(exactly = 0) { searchPostsUseCase("k") }
            @Suppress("UNUSED_EXPRESSION")
            verify(exactly = 0) { searchPostsUseCase("ko") }
            @Suppress("UNUSED_EXPRESSION")
            verify(exactly = 1) { searchPostsUseCase("kot") }
        }

    @Test
    fun `onSearchQueryChange with blank query exits search mode and reloads all posts`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = createViewModel()

            // Given — activate search first
            viewModel.onSearchQueryChange("kotlin")
            advanceUntilIdle()
            assertTrue("Search mode should be active", viewModel.uiState.value.isSearchActive)

            // When — user clears the search bar
            viewModel.onSearchQueryChange("")
            advanceUntilIdle()

            // Then
            assertFalse(viewModel.uiState.value.isSearchActive)
            assertEquals("", viewModel.uiState.value.searchQuery)
            // getPostsUseCase: once on init + once when blank query triggers loadPosts()
            @Suppress("UNUSED_EXPRESSION")
            verify(atLeast = 2) { getPostsUseCase() }
        }

    // ─── clearSearch() ─────────────────────────────────────────────────────

    @Test
    fun `clearSearch resets query and exits search mode`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val viewModel = createViewModel()

            // Given — activate search
            viewModel.onSearchQueryChange("test")
            advanceUntilIdle()
            assertTrue("Search mode should be active", viewModel.uiState.value.isSearchActive)

            // When
            viewModel.clearSearch()
            advanceUntilIdle()

            // Then
            assertEquals("", viewModel.uiState.value.searchQuery)
            assertFalse(viewModel.uiState.value.isSearchActive)
        }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun createViewModel() = PostsViewModel(getPostsUseCase, searchPostsUseCase)

    private fun fakePost(id: Int = 1, title: String = "Post $id") = Post(
        id = id, userId = 1, title = title, body = "Body $id"
    )
}
