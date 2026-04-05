package com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase

import app.cash.turbine.test
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.domain.repository.CommentRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GetCommentsUseCase].
 *
 * Verifies delegation to [CommentRepository] with the correct postId,
 * and that all result states (Loading, Success, Error) are propagated unchanged.
 */
class GetCommentsUseCaseTest {

    private val repository: CommentRepository = mockk()
    private val useCase = GetCommentsUseCase(repository)

    @Test
    fun `invoke delegates to repository with the correct postId`() = runTest {
        // Given
        val comments = listOf(fakeComment(id = 1, postId = 5))
        every { repository.getCommentsByPostId(5) } returns flowOf(Result.Success(comments))

        // When / Then
        useCase(5).test {
            val result = awaitItem() as Result.Success
            assertEquals(comments, result.data)
            awaitComplete()
        }
        verify(exactly = 1) { repository.getCommentsByPostId(5) }
    }

    @Test
    fun `invoke propagates loading state from repository`() = runTest {
        // Given
        every { repository.getCommentsByPostId(any()) } returns flowOf(
            Result.Loading,
            Result.Success(emptyList())
        )

        // When / Then
        useCase(1).test {
            assertEquals(Result.Loading, awaitItem())
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        // Given
        every { repository.getCommentsByPostId(any()) } returns flowOf(
            Result.Error("Failed to load comments")
        )

        // When / Then
        useCase(1).test {
            val error = awaitItem() as Result.Error
            assertEquals("Failed to load comments", error.message)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty success when post has no comments`() = runTest {
        // Given
        every { repository.getCommentsByPostId(any()) } returns flowOf(Result.Success(emptyList()))

        // When / Then
        useCase(1).test {
            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())
            awaitComplete()
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun fakeComment(id: Int = 1, postId: Int = 1) = Comment(
        id = id, postId = postId, name = "Author $id", body = "Body $id"
    )
}
