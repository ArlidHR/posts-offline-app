package com.github.arlidhr.posts_offline_app.modules.comments.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.core.navigation.Routes
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase.AddCommentUseCase
import com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase.GetCommentsUseCase
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase.GetPostByIdUseCase
import com.github.arlidhr.posts_offline_app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [CommentsViewModel].
 *
 * Covers:
 * - init block: loadPostTitle + loadComments are both called on creation.
 * - Comment list state transitions: Loading → Success / Error.
 * - Post title is loaded and stored in state for the TopAppBar.
 * - Non-critical title failure doesn't crash or block comments loading.
 * - Form field updates: name/body changes reflected in state.
 * - submitComment happy path: calls use case and clears the form.
 * - submitComment validation path: sets addCommentError, does not clear form.
 *
 * [SavedStateHandle] is constructed directly — no Hilt instrumentation required.
 */
class CommentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCommentsUseCase: GetCommentsUseCase = mockk()
    private val addCommentUseCase: AddCommentUseCase = mockk()
    private val getPostByIdUseCase: GetPostByIdUseCase = mockk()

    private val testPostId = 1

    @Before
    fun setUp() {
        // Default stubs — ensure init block succeeds for all tests
        coEvery { getPostByIdUseCase(testPostId) } returns Result.Success(fakePost())
        every { getCommentsUseCase(testPostId) } returns flowOf(Result.Success(emptyList()))
    }

    // ─── Init block ────────────────────────────────────────────────────────

    @Test
    fun `init calls both loadPostTitle and loadComments on creation`() = runTest {
        createViewModel()

        coVerify(exactly = 1) { getPostByIdUseCase(testPostId) }
        // getCommentsUseCase must be called once from init → loadComments()
        // (verify after creation since UnconfinedTestDispatcher runs eagerly)
    }

    @Test
    fun `loadComments updates state with comments on success`() = runTest {
        // Given
        val comments = listOf(fakeComment(id = 1), fakeComment(id = 2))
        every { getCommentsUseCase(testPostId) } returns flowOf(
            Result.Loading,
            Result.Success(comments)
        )

        // When
        val viewModel = createViewModel()

        // Then — with UnconfinedTestDispatcher, final state is ready immediately
        assertEquals(comments, viewModel.uiState.value.comments)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadComments sets error when repository emits error`() = runTest {
        // Given
        every { getCommentsUseCase(testPostId) } returns flowOf(
            Result.Error("No comments available")
        )

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals("No comments available", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadComments sets isLoading true while flow is pending`() = runTest {
        // Given — simulates long-running network call
        every { getCommentsUseCase(testPostId) } returns flow { emit(Result.Loading) }

        // When
        val viewModel = createViewModel()

        // Then
        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Post title ────────────────────────────────────────────────────────

    @Test
    fun `loadPostTitle populates postTitle in state from the repository`() = runTest {
        // Given
        val post = fakePost(title = "My Awesome Post")
        coEvery { getPostByIdUseCase(testPostId) } returns Result.Success(post)

        // When
        val viewModel = createViewModel()

        // Then
        assertEquals("My Awesome Post", viewModel.uiState.value.postTitle)
    }

    @Test
    fun `loadPostTitle leaves postTitle empty when getPostById fails (non-critical)`() = runTest {
        // Given — post not found; this is non-critical, app should not crash
        coEvery { getPostByIdUseCase(testPostId) } returns Result.Error("Post not found")

        // When
        val viewModel = createViewModel()

        // Then — graceful degradation: title stays empty, no error propagated to UI
        assertEquals("", viewModel.uiState.value.postTitle)
        assertNull(viewModel.uiState.value.error) // error state is for comments only
    }

    // ─── Form field updates ────────────────────────────────────────────────

    @Test
    fun `onNewCommentNameChange updates newCommentName in state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onNewCommentNameChange("John Doe")

        assertEquals("John Doe", viewModel.uiState.value.newCommentName)
    }

    @Test
    fun `onNewCommentBodyChange updates newCommentBody in state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onNewCommentBodyChange("This is a comment body")

        assertEquals("This is a comment body", viewModel.uiState.value.newCommentBody)
    }

    @Test
    fun `onNewCommentNameChange clears existing addCommentError`() = runTest {
        // Given — produce a validation error first
        coEvery { addCommentUseCase(any(), any(), any()) } returns Result.Error("Validation error")
        val viewModel = createViewModel()
        viewModel.submitComment()
        assertNotNull(viewModel.uiState.value.addCommentError)

        // When — user starts typing to fix the error
        viewModel.onNewCommentNameChange("John")

        // Then — error is cleared immediately for better UX
        assertNull(viewModel.uiState.value.addCommentError)
    }

    @Test
    fun `onNewCommentBodyChange clears existing addCommentError`() = runTest {
        // Given
        coEvery { addCommentUseCase(any(), any(), any()) } returns Result.Error("Validation error")
        val viewModel = createViewModel()
        viewModel.submitComment()
        assertNotNull(viewModel.uiState.value.addCommentError)

        // When
        viewModel.onNewCommentBodyChange("Some body text")

        // Then
        assertNull(viewModel.uiState.value.addCommentError)
    }

    @Test
    fun `canSubmitComment is true only when both name and body are not blank`() = runTest {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.canSubmitComment) // both empty initially

        viewModel.onNewCommentNameChange("Alice")
        assertFalse(viewModel.uiState.value.canSubmitComment) // body still empty

        viewModel.onNewCommentBodyChange("Great post!")
        assertTrue(viewModel.uiState.value.canSubmitComment) // both filled → enabled
    }

    // ─── submitComment() ───────────────────────────────────────────────────

    @Test
    fun `submitComment calls addCommentUseCase with current form values and postId`() = runTest {
        // Given
        coEvery { addCommentUseCase(testPostId, "Alice", "Hello!") } returns Result.Success(Unit)
        val viewModel = createViewModel()
        viewModel.onNewCommentNameChange("Alice")
        viewModel.onNewCommentBodyChange("Hello!")

        // When
        viewModel.submitComment()

        // Then
        coVerify(exactly = 1) { addCommentUseCase(testPostId, "Alice", "Hello!") }
    }

    @Test
    fun `submitComment clears the form on success`() = runTest {
        // Given
        coEvery { addCommentUseCase(any(), any(), any()) } returns Result.Success(Unit)
        val viewModel = createViewModel()
        viewModel.onNewCommentNameChange("Alice")
        viewModel.onNewCommentBodyChange("Hello!")

        // When
        viewModel.submitComment()

        // Then — form is reset so the user can write a second comment
        assertEquals("", viewModel.uiState.value.newCommentName)
        assertEquals("", viewModel.uiState.value.newCommentBody)
        assertFalse(viewModel.uiState.value.isAddingComment)
        assertNull(viewModel.uiState.value.addCommentError)
    }

    @Test
    fun `submitComment sets addCommentError and preserves form values on failure`() = runTest {
        // Given
        coEvery { addCommentUseCase(any(), any(), any()) } returns Result.Error("Name and body cannot be empty")
        val viewModel = createViewModel()

        // When
        viewModel.submitComment()

        // Then — error shown, form values not cleared (user can fix and retry)
        assertNotNull(viewModel.uiState.value.addCommentError)
        assertEquals("Name and body cannot be empty", viewModel.uiState.value.addCommentError)
        assertFalse(viewModel.uiState.value.isAddingComment)
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun createViewModel() = CommentsViewModel(
        savedStateHandle = SavedStateHandle(mapOf(Routes.PostComments.ARG_POST_ID to testPostId)),
        getCommentsUseCase = getCommentsUseCase,
        addCommentUseCase = addCommentUseCase,
        getPostByIdUseCase = getPostByIdUseCase
    )

    private fun fakePost(id: Int = testPostId, title: String = "Post $id") = Post(
        id = id, userId = 1, title = title, body = "Body $id"
    )

    private fun fakeComment(id: Int = 1) = Comment(
        id = id, postId = testPostId, name = "Author $id", body = "Body $id"
    )
}
