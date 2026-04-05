package com.github.arlidhr.posts_offline_app.modules.posts.data.repository

import com.github.arlidhr.posts_offline_app.core.di.IoDispatcher
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostLocalDataSource
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostRemoteDataSource
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first implementation of [PostRepository].
 *
 * Strategy (cache-then-network):
 * 1. Emit [Result.Loading]
 * 2. Collect from local database (Room Flow)
 * 3. On first local emission → attempt API refresh in background
 * 4. Room automatically re-emits after refresh writes new data
 * 5. If API fails AND local cache is empty → emit [Result.Error]
 * 6. If API fails but cache has data → emit cached data silently (user sees stale data)
 *
 * This ensures the UI always shows data as fast as possible,
 * and updates seamlessly when fresh data arrives from the network.
 */
class PostRepositoryImpl @Inject constructor(
    private val localDataSource: PostLocalDataSource,
    private val remoteDataSource: PostRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PostRepository {

    override fun getPosts(): Flow<Result<List<Post>>> = flow {
        emit(Result.Loading)

        // Attempt to refresh from API first
        val refreshError = tryRefreshFromApi()

        // Collect local data — Room Flow will emit current data + future updates
        localDataSource.getAllPosts().collect { entities ->
            val posts = entities.map(PostEntity::toDomain)

            if (posts.isEmpty() && refreshError != null) {
                // No cache AND API failed → surface the error
                emit(Result.Error(
                    message = refreshError.message ?: "Failed to load posts",
                    throwable = refreshError
                ))
            } else {
                emit(Result.Success(posts))
            }
        }
    }.flowOn(ioDispatcher)

    override fun searchPosts(query: String): Flow<Result<List<Post>>> =
        localDataSource.searchPosts(query)
            .map<List<PostEntity>, Result<List<Post>>> { entities ->
                Result.Success(entities.map(PostEntity::toDomain))
            }
            .catch { e ->
                emit(Result.Error(
                    message = e.message ?: "Search failed",
                    throwable = e
                ))
            }
            .flowOn(ioDispatcher)

    override suspend fun getPostById(id: Int): Result<Post> {
        return try {
            val entity = localDataSource.getPostById(id)
            if (entity != null) {
                Result.Success(entity.toDomain())
            } else {
                Result.Error("Post with ID $id not found")
            }
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to get post",
                throwable = e
            )
        }
    }

    override suspend fun refreshPosts(): Result<Unit> {
        return try {
            val remotePosts = remoteDataSource.getPosts()
            localDataSource.refreshPosts(remotePosts)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to refresh posts",
                throwable = e
            )
        }
    }

    /**
     * Attempts to fetch posts from the API and store them locally.
     * Returns the exception if it fails, or null on success.
     *
     * This is intentionally a non-throwing helper — the caller decides
     * how to handle the error based on the local cache state.
     */
    private suspend fun tryRefreshFromApi(): Throwable? {
        return try {
            val remotePosts = remoteDataSource.getPosts()
            localDataSource.refreshPosts(remotePosts)
            null
        } catch (e: Exception) {
            e
        }
    }
}
