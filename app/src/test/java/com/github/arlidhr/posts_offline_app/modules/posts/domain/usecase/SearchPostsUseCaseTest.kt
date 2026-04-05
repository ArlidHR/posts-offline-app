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
 * Unit tests for [SearchPostsUseCase].
 *
 * Key behaviors under test:
 * - Blank / whitespace queries return an empty [Result.Success] immediately,
 *   without touching the repository (pure client-side guard).
 * - Valid queries are trimmed before being forwarded to the repository.
 * - Error states from the repository are propagated unchanged.
 *
 * Fulfills functional requirement:
 * "búsqueda por Nombre o por ID" — offline, against the local Room cache.
 */
class SearchPostsUseCaseTest {

    private val repository: PostRepository = mockk()
    private val useCase = SearchPostsUseCase(repository)

    // ─── Blank query guard ─────────────────────────────────────────────────

    @Test
    fun `invoke with empty string returns empty success without calling repository`() = runTest {
        useCase("").test {
            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())
            awaitComplete()
        }
        verify(exactly = 0) { repository.searchPosts(any()) }
    }

    @Test
    fun `invoke with whitespace-only query returns empty success without calling repository`() = runTest {
        useCase("   ").test {
            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())
            awaitComplete()
        }
        verify(exactly = 0) { repository.searchPosts(any()) }
    }

    // ─── Whitespace trimming ───────────────────────────────────────────────

    @Test
    fun `invoke trims leading and trailing whitespace before calling repository`() = runTest {
        // Given
        every { repository.searchPosts("kotlin") } returns flowOf(Result.Success(emptyList()))

        // When
        useCase("  kotlin  ").test {
            awaitItem()
            awaitComplete()
        }

        // Then — repository receives the trimmed query
        verify(exactly = 1) { repository.searchPosts("kotlin") }
        verify(exactly = 0) { repository.searchPosts("  kotlin  ") }
    }

    // ─── Delegation & data mapping ─────────────────────────────────────────

    @Test
    fun `invoke with valid query delegates to repository and emits matching posts`() = runTest {
        // Given
        val posts = listOf(fakePost(title = "Kotlin Coroutines"))
        every { repository.searchPosts("kotlin") } returns flowOf(Result.Success(posts))

        // When / Then
        useCase("kotlin").test {
            val result = awaitItem() as Result.Success
            assertEquals(posts, result.data)
            awaitComplete()
        }
        verify(exactly = 1) { repository.searchPosts("kotlin") }
    }

    @Test
    fun `invoke with numeric id string delegates to repository`() = runTest {
        // Given — user searches by post ID
        val posts = listOf(fakePost(id = 5))
        every { repository.searchPosts("5") } returns flowOf(Result.Success(posts))

        // When / Then
        useCase("5").test {
            val result = awaitItem() as Result.Success
            assertEquals(1, result.data.size)
            assertEquals(5, result.data[0].id)
            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates error from repository`() = runTest {
        // Given
        every { repository.searchPosts(any()) } returns flowOf(Result.Error("Search failed"))

        // When / Then
        useCase("test").test {
            val error = awaitItem() as Result.Error
            assertEquals("Search failed", error.message)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty success when repository finds no matches`() = runTest {
        // Given
        every { repository.searchPosts(any()) } returns flowOf(Result.Success(emptyList()))

        // When / Then
        useCase("nonexistent").test {
            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())
            awaitComplete()
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun fakePost(id: Int = 1, title: String = "Post $id") = Post(
        id = id, userId = 1, title = title, body = "Body $id"
    )
}
