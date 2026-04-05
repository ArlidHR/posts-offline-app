package com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase

import app.cash.turbine.test
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [GetPostsUseCase].
 *
 * Verifies that the use case acts as a clean pass-through to [PostRepository],
 * delegating the flow exactly as returned — no transformation, no side effects.
 */
class GetPostsUseCaseTest {

    private val repository: PostRepository = mockk()
    private val useCase = GetPostsUseCase(repository)

    @Test
    fun `invoke delegates call to repository and emits success`() = runTest {
        // Given
        val posts = listOf(fakePost(id = 1), fakePost(id = 2))
        every { repository.getPosts() } returns flowOf(Result.Success(posts))

        // When / Then
        useCase().test {
            assertEquals(Result.Success(posts), awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { repository.getPosts() }
    }

    @Test
    fun `invoke propagates loading state from repository`() = runTest {
        // Given
        every { repository.getPosts() } returns flowOf(
            Result.Loading,
            Result.Success(emptyList())
        )

        // When / Then
        useCase().test {
            assertEquals(Result.Loading, awaitItem())
            assertEquals(Result.Success(emptyList<Post>()), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates error state from repository`() = runTest {
        // Given
        val errorMessage = "Network failure"
        every { repository.getPosts() } returns flowOf(Result.Error(errorMessage))

        // When / Then
        useCase().test {
            val error = awaitItem() as Result.Error
            assertEquals(errorMessage, error.message)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits empty success when repository returns no posts`() = runTest {
        // Given
        every { repository.getPosts() } returns flowOf(Result.Success(emptyList()))

        // When / Then
        useCase().test {
            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())
            awaitComplete()
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun fakePost(id: Int = 1) = Post(
        id = id,
        userId = 1,
        title = "Post title $id",
        body = "Post body $id"
    )
}
