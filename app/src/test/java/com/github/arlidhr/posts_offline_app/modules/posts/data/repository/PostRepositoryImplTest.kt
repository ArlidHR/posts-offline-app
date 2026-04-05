package com.github.arlidhr.posts_offline_app.modules.posts.data.repository

import app.cash.turbine.test
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostLocalDataSource
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostRemoteDataSource
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
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
 * Unit tests for [PostRepositoryImpl].
 *
 * Strategy under test (cache-then-network / offline-first):
 *  1. Always emit [Result.Loading] first.
 *  2. Attempt API refresh in background.
 *  3. Collect from Room Flow → emit cached or fresh data.
 *  4. If API fails AND cache is empty → emit [Result.Error].
 *  5. If API fails BUT cache has data → silently show stale data.
 *
 * [UnconfinedTestDispatcher] is injected as the IO dispatcher so that
 * [kotlinx.coroutines.flow.flowOn] does not change execution context in tests,
 * keeping all assertions synchronous and deterministic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryImplTest {

    private val localDataSource: PostLocalDataSource = mockk()
    private val remoteDataSource: PostRemoteDataSource = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: PostRepositoryImpl

    @Before
    fun setUp() {
        repository = PostRepositoryImpl(localDataSource, remoteDataSource, testDispatcher)
    }

    // ─── getPosts() ────────────────────────────────────────────────────────

    @Test
    fun `getPosts emits loading then success when api succeeds and cache has data`() = runTest {
        // Given
        val entities = listOf(fakePostEntity(id = 1), fakePostEntity(id = 2))
        coEvery { remoteDataSource.getPosts() } returns entities
        coEvery { localDataSource.refreshPosts(entities) } just Runs
        every { localDataSource.getAllPosts() } returns flowOf(entities)

        // When / Then
        repository.getPosts().test {
            assertEquals(Result.Loading, awaitItem())

            val success = awaitItem() as Result.Success
            assertEquals(2, success.data.size)
            assertEquals(1, success.data[0].id)
            assertEquals(2, success.data[1].id)

            awaitComplete()
        }
    }

    @Test
    fun `getPosts emits loading then error when api fails and cache is empty`() = runTest {
        // Given — no internet, no cache
        coEvery { remoteDataSource.getPosts() } throws IOException("No internet connection")
        every { localDataSource.getAllPosts() } returns flowOf(emptyList())

        // When / Then
        repository.getPosts().test {
            assertEquals(Result.Loading, awaitItem())

            val error = awaitItem() as Result.Error
            assertTrue("Error message should not be blank", error.message.isNotBlank())

            awaitComplete()
        }
    }

    @Test
    fun `getPosts shows cached data when api fails but local cache is not empty`() = runTest {
        // Given — offline with stale cache (expected UX: show old data, no error)
        val cachedEntities = listOf(fakePostEntity(id = 10), fakePostEntity(id = 11))
        coEvery { remoteDataSource.getPosts() } throws IOException("Timeout")
        every { localDataSource.getAllPosts() } returns flowOf(cachedEntities)

        // When / Then — cache is surfaced, no error shown to user
        repository.getPosts().test {
            assertEquals(Result.Loading, awaitItem())

            val success = awaitItem() as Result.Success
            assertEquals(2, success.data.size)

            awaitComplete()
        }
    }

    @Test
    fun `getPosts maps PostEntity to Post domain model correctly`() = runTest {
        // Given
        val entity = fakePostEntity(id = 5, userId = 2, title = "Mapped title", body = "Mapped body")
        coEvery { remoteDataSource.getPosts() } returns listOf(entity)
        coEvery { localDataSource.refreshPosts(any()) } just Runs
        every { localDataSource.getAllPosts() } returns flowOf(listOf(entity))

        // When / Then
        repository.getPosts().test {
            awaitItem() // Loading
            val success = awaitItem() as Result.Success
            with(success.data[0]) {
                assertEquals(5, id)
                assertEquals(2, userId)
                assertEquals("Mapped title", title)
                assertEquals("Mapped body", body)
            }
            awaitComplete()
        }
    }

    // ─── searchPosts() ─────────────────────────────────────────────────────

    @Test
    fun `searchPosts maps entities to domain models and emits success`() = runTest {
        // Given
        val entities = listOf(fakePostEntity(title = "Kotlin coroutines"))
        every { localDataSource.searchPosts("kotlin") } returns flowOf(entities)

        // When / Then
        repository.searchPosts("kotlin").test {
            val result = awaitItem() as Result.Success
            assertEquals(1, result.data.size)
            assertEquals("Kotlin coroutines", result.data[0].title)
            awaitComplete()
        }
    }

    @Test
    fun `searchPosts returns empty success when no results match query`() = runTest {
        // Given
        every { localDataSource.searchPosts("xyz") } returns flowOf(emptyList())

        // When / Then
        repository.searchPosts("xyz").test {
            val result = awaitItem() as Result.Success
            assertTrue(result.data.isEmpty())
            awaitComplete()
        }
    }

    // ─── getPostById() ─────────────────────────────────────────────────────

    @Test
    fun `getPostById returns success when entity is found in local cache`() = runTest {
        // Given
        val entity = fakePostEntity(id = 5)
        coEvery { localDataSource.getPostById(5) } returns entity

        // When
        val result = repository.getPostById(5) as Result.Success

        // Then
        assertEquals(5, result.data.id)
    }

    @Test
    fun `getPostById returns error with id in message when entity is not found`() = runTest {
        // Given
        coEvery { localDataSource.getPostById(99) } returns null

        // When
        val result = repository.getPostById(99) as Result.Error

        // Then — message mentions the missing ID for traceability
        assertTrue("Error message should contain the ID", result.message.contains("99"))
    }

    @Test
    fun `getPostById returns error when local data source throws exception`() = runTest {
        // Given
        coEvery { localDataSource.getPostById(any()) } throws RuntimeException("DB corrupted")

        // When
        val result = repository.getPostById(1) as Result.Error

        // Then
        assertEquals("DB corrupted", result.message)
    }

    // ─── refreshPosts() ────────────────────────────────────────────────────

    @Test
    fun `refreshPosts returns success and stores fetched posts locally`() = runTest {
        // Given
        val entities = listOf(fakePostEntity(id = 1))
        coEvery { remoteDataSource.getPosts() } returns entities
        coEvery { localDataSource.refreshPosts(entities) } just Runs

        // When
        val result = repository.refreshPosts()

        // Then
        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { localDataSource.refreshPosts(entities) }
    }

    @Test
    fun `refreshPosts returns error when remote api throws`() = runTest {
        // Given
        coEvery { remoteDataSource.getPosts() } throws IOException("Server unreachable")

        // When
        val result = repository.refreshPosts() as Result.Error

        // Then
        assertEquals("Server unreachable", result.message)
    }

    @Test
    fun `refreshPosts does not call local storage when remote api fails`() = runTest {
        // Given
        coEvery { remoteDataSource.getPosts() } throws IOException("Timeout")

        // When
        repository.refreshPosts()

        // Then — no attempt to persist when fetch fails
        coVerify(exactly = 0) { localDataSource.refreshPosts(any()) }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private fun fakePostEntity(
        id: Int = 1,
        userId: Int = 1,
        title: String = "Post $id",
        body: String = "Body $id"
    ) = PostEntity(id = id, userId = userId, title = title, body = body)
}
