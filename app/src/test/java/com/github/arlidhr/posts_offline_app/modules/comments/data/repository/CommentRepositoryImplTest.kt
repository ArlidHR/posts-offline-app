package com.github.arlidhr.posts_offline_app.modules.comments.data.repository

import app.cash.turbine.test
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentLocalDataSource
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentRemoteDataSource
import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for [CommentRepositoryImpl].
 *
 * Strategy under test:
 * - On getCommentsByPostId: emit Loading, try API refresh (delete remote → insert fresh),
 *   collect Room Flow that includes both API-cached and user-created (local) comments.
 * - On addLocalComment: insert entity with id = 0 (auto-generate) and isLocal = true.
 *
 * Key invariant: local user-created comments (isLocal = true) are NEVER deleted
 * during an API refresh cycle — they persist across network calls.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CommentRepositoryImplTest {

    private val localDataSource: CommentLocalDataSource = mockk()
    private val remoteDataSource: CommentRemoteDataSource = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: CommentRepositoryImpl

    @Before
    fun setUp() {
        repository = CommentRepositoryImpl(localDataSource, remoteDataSource, testDispatcher)
    }

    // ─── getCommentsByPostId() ─────────────────────────────────────────────

    @Test
    fun `getCommentsByPostId emits loading then success with api and local comments`() = runTest {
        // Given
        val apiEntities = listOf(fakeEntity(id = 1, isLocal = false))
        val localEntities = listOf(fakeEntity(id = 2, isLocal = true))
        val allEntities = apiEntities + localEntities

        coEvery { remoteDataSource.getCommentsByPostId(1) } returns apiEntities
        coEvery { localDataSource.deleteRemoteCommentsByPostId(1) } just Runs
        coEvery { localDataSource.insertAll(apiEntities) } just Runs
        every { localDataSource.getCommentsByPostId(1) } returns flowOf(allEntities)

        // When / Then
        repository.getCommentsByPostId(1).test {
            assertEquals(Result.Loading, awaitItem())

            val success = awaitItem() as Result.Success
            assertEquals(2, success.data.size)

            awaitComplete()
        }
    }

    @Test
    fun `getCommentsByPostId emits loading then error when api fails and cache is empty`() = runTest {
        // Given — worst case: no internet, no cache
        coEvery { remoteDataSource.getCommentsByPostId(any()) } throws IOException("No internet")
        every { localDataSource.getCommentsByPostId(any()) } returns flowOf(emptyList())

        // When / Then
        repository.getCommentsByPostId(1).test {
            assertEquals(Result.Loading, awaitItem())

            val error = awaitItem() as Result.Error
            assertTrue(error.message.isNotBlank())

            awaitComplete()
        }
    }

    @Test
    fun `getCommentsByPostId shows stale cache when api fails but cache has data`() = runTest {
        // Given — offline but cached (expected UX: show old data)
        val cached = listOf(fakeEntity(id = 1, isLocal = true), fakeEntity(id = 2, isLocal = false))
        coEvery { remoteDataSource.getCommentsByPostId(any()) } throws IOException("Offline")
        every { localDataSource.getCommentsByPostId(any()) } returns flowOf(cached)

        // When / Then
        repository.getCommentsByPostId(1).test {
            assertEquals(Result.Loading, awaitItem())

            val success = awaitItem() as Result.Success
            assertEquals(2, success.data.size)

            awaitComplete()
        }
    }

    @Test
    fun `getCommentsByPostId deletes remote cache then inserts fresh api data (preserves local)`() = runTest {
        // Given
        val apiEntities = listOf(fakeEntity(id = 10, isLocal = false))
        coEvery { remoteDataSource.getCommentsByPostId(1) } returns apiEntities
        coEvery { localDataSource.deleteRemoteCommentsByPostId(1) } just Runs
        coEvery { localDataSource.insertAll(apiEntities) } just Runs
        every { localDataSource.getCommentsByPostId(1) } returns flowOf(apiEntities)

        // When
        repository.getCommentsByPostId(1).test {
            skipItems(2)  // Loading + Success
            awaitComplete()
        }

        // Then — refresh cycle must follow the correct order:
        // 1. delete stale remote comments, 2. insert fresh ones (local comments untouched)
        coVerifyOrder {
            localDataSource.deleteRemoteCommentsByPostId(1)
            localDataSource.insertAll(apiEntities)
        }
    }

    @Test
    fun `getCommentsByPostId maps CommentEntity to Comment domain model correctly`() = runTest {
        // Given
        val entity = fakeEntity(id = 7, postId = 2, name = "Alice", body = "Insightful!", isLocal = true)
        coEvery { remoteDataSource.getCommentsByPostId(2) } returns emptyList()
        coEvery { localDataSource.deleteRemoteCommentsByPostId(2) } just Runs
        coEvery { localDataSource.insertAll(any()) } just Runs
        every { localDataSource.getCommentsByPostId(2) } returns flowOf(listOf(entity))

        // When / Then
        repository.getCommentsByPostId(2).test {
            awaitItem() // Loading
            val success = awaitItem() as Result.Success
            with(success.data[0]) {
                assertEquals(7, id)
                assertEquals(2, postId)
                assertEquals("Alice", name)
                assertEquals("Insightful!", body)
                assertTrue(isLocal)
            }
            awaitComplete()
        }
    }

    // ─── addLocalComment() ────────────────────────────────────────────────

    @Test
    fun `addLocalComment inserts entity with id 0 for auto-generation and isLocal true`() = runTest {
        // Given
        val capturedEntity = slot<CommentEntity>()
        coEvery { localDataSource.insert(capture(capturedEntity)) } just Runs

        val comment = fakeComment(postId = 3)

        // When
        val result = repository.addLocalComment(comment)

        // Then
        assertTrue(result is Result.Success)
        with(capturedEntity.captured) {
            assertEquals(0, id)         // id = 0 → SQLite auto-generates
            assertTrue(isLocal)
            assertEquals(3, postId)
        }
        coVerify(exactly = 1) { localDataSource.insert(any()) }
    }

    @Test
    fun `addLocalComment returns error when local data source throws exception`() = runTest {
        // Given
        coEvery { localDataSource.insert(any()) } throws RuntimeException("DB write failed")

        // When
        val result = repository.addLocalComment(fakeComment()) as Result.Error

        // Then
        assertEquals("DB write failed", result.message)
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun fakeEntity(
        id: Int = 1,
        postId: Int = 1,
        name: String = "Author $id",
        body: String = "Body $id",
        isLocal: Boolean = false
    ) = CommentEntity(
        id = id,
        postId = postId,
        name = name,
        email = "author$id@test.com",
        body = body,
        isLocal = isLocal
    )

    private fun fakeComment(postId: Int = 1) = Comment(
        id = 1,
        postId = postId,
        name = "Author",
        email = "author@test.com",
        body = "Body",
        isLocal = false
    )
}
