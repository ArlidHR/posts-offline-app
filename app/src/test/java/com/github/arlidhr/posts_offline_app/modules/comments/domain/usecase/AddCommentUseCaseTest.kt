package com.github.arlidhr.posts_offline_app.modules.comments.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.domain.repository.CommentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [AddCommentUseCase].
 *
 * Validation rules under test:
 * - Blank name or blank body → [Result.Error] immediately, repository is NOT called.
 * - Whitespace-only inputs are treated as blank (trim check).
 * - Valid inputs are trimmed and forwarded as a [Comment] with [Comment.isLocal] = true.
 *
 * Fulfills functional requirement:
 * "crear N comentarios a dicha publicación" — stored locally, never sent to the API.
 */
class AddCommentUseCaseTest {

    private val repository: CommentRepository = mockk()
    private val useCase = AddCommentUseCase(repository)

    // ─── Input validation ──────────────────────────────────────────────────

    @Test
    fun `invoke with empty name returns error without calling repository`() = runTest {
        // When
        val result = useCase(postId = 1, name = "", body = "Valid body")

        // Then
        assertTrue(result is Result.Error)
        assertEquals("Name and body cannot be empty", (result as Result.Error).message)
        coVerify(exactly = 0) { repository.addLocalComment(any()) }
    }

    @Test
    fun `invoke with whitespace-only name returns error without calling repository`() = runTest {
        val result = useCase(postId = 1, name = "   ", body = "Valid body")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { repository.addLocalComment(any()) }
    }

    @Test
    fun `invoke with empty body returns error without calling repository`() = runTest {
        val result = useCase(postId = 1, name = "Valid name", body = "")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { repository.addLocalComment(any()) }
    }

    @Test
    fun `invoke with whitespace-only body returns error without calling repository`() = runTest {
        val result = useCase(postId = 1, name = "Valid name", body = "   ")

        assertTrue(result is Result.Error)
        coVerify(exactly = 0) { repository.addLocalComment(any()) }
    }

    // ─── Happy path ────────────────────────────────────────────────────────

    @Test
    fun `invoke with valid inputs creates comment marked as local and delegates to repository`() =
        runTest {
            // Given
            val capturedComment = slot<Comment>()
            coEvery { repository.addLocalComment(capture(capturedComment)) } returns Result.Success(Unit)

            // When
            val result = useCase(postId = 3, name = "John", body = "Great post!")

            // Then — repository is called once with correctly built comment
            assertTrue(result is Result.Success)
            coVerify(exactly = 1) { repository.addLocalComment(any()) }

            with(capturedComment.captured) {
                assertEquals(3, postId)
                assertEquals("John", name)
                assertEquals("Great post!", body)
                assertTrue("Comment must be flagged as local", isLocal)
            }
        }

    @Test
    fun `invoke trims whitespace from name and body before persisting`() = runTest {
        // Given
        val capturedComment = slot<Comment>()
        coEvery { repository.addLocalComment(capture(capturedComment)) } returns Result.Success(Unit)

        // When
        useCase(postId = 1, name = "  John Doe  ", body = "  Hello world  ")

        // Then — trimmed values are saved, not the raw inputs
        assertEquals("John Doe", capturedComment.captured.name)
        assertEquals("Hello world", capturedComment.captured.body)
    }

    @Test
    fun `invoke returns error when repository fails to persist`() = runTest {
        // Given
        coEvery { repository.addLocalComment(any()) } returns Result.Error("Disk full")

        // When
        val result = useCase(postId = 1, name = "Name", body = "Body") as Result.Error

        // Then
        assertEquals("Disk full", result.message)
    }
}
