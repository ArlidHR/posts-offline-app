package com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GetPostByIdUseCase].
 *
 * This use case fetches a cached post by ID from the local Room database.
 * It is used on the Comments screen to display the post's title in the TopAppBar.
 */
class GetPostByIdUseCaseTest {

    private val repository: PostRepository = mockk()
    private val useCase = GetPostByIdUseCase(repository)

    @Test
    fun `invoke returns success when post exists in local cache`() = runTest {
        // Given
        val post = fakePost(id = 42)
        coEvery { repository.getPostById(42) } returns Result.Success(post)

        // When
        val result = useCase(42)

        // Then
        assertEquals(Result.Success(post), result)
        coVerify(exactly = 1) { repository.getPostById(42) }
    }

    @Test
    fun `invoke returns error when post is not found in local cache`() = runTest {
        // Given
        coEvery { repository.getPostById(99) } returns Result.Error("Post with ID 99 not found")

        // When
        val result = useCase(99) as Result.Error

        // Then — error message contains the ID for debuggability
        assertTrue(result.message.contains("99"))
    }

    @Test
    fun `invoke forwards the exact id to the repository`() = runTest {
        // Given
        val targetId = 7
        coEvery { repository.getPostById(targetId) } returns Result.Success(fakePost(id = targetId))

        // When
        useCase(targetId)

        // Then — exact id is passed, not a default or transformed value
        coVerify { repository.getPostById(targetId) }
    }

    @Test
    fun `invoke propagates repository error as-is`() = runTest {
        // Given
        coEvery { repository.getPostById(any()) } returns Result.Error("Database error")

        // When
        val result = useCase(1) as Result.Error

        // Then
        assertEquals("Database error", result.message)
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun fakePost(id: Int = 1) = Post(
        id = id, userId = 1, title = "Post $id", body = "Body $id"
    )
}
