package com.github.arlidhr.posts_offline_app.modules.comments.data.repository

import com.github.arlidhr.posts_offline_app.core.di.IoDispatcher
import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentLocalDataSource
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentRemoteDataSource
import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.toEntity
import com.github.arlidhr.posts_offline_app.modules.comments.domain.model.Comment
import com.github.arlidhr.posts_offline_app.modules.comments.domain.repository.CommentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


/**
 * Offline-first implementation of [CommentRepository].
 *
 * Strategy for getCommentsByPostId (cache-then-network):
 * 1. Emit Loading
 * 2. Try API refresh → delete old remote comments → insert fresh ones (preserves local)
 * 3. Collect Room Flow → emit all comments (API + local)
 * 4. If API fails AND cache is empty → emit Error
 *
 * Key difference from Posts: only remote comments are refreshed on API call.
 * Local user-created comments (isLocal = true) are always preserved.
 */
class CommentRepositoryImpl @Inject constructor(
    private val localDataSource: CommentLocalDataSource,
    private val remoteDataSource: CommentRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CommentRepository {

    override fun getCommentsByPostId(postId: Int): Flow<Result<List<Comment>>> = flow {
        emit(Result.Loading)

        // Attempt API refresh (preserves local comments)
        val refreshError = tryRefreshFromApi(postId)

        // Collect local data — includes both API-cached and user-created comments
        localDataSource.getCommentsByPostId(postId).collect { entities ->
            val comments = entities.map(CommentEntity::toDomain)

            if (comments.isEmpty() && refreshError != null) {
                emit(Result.Error(
                    message = refreshError.message ?: "Failed to load comments",
                    throwable = refreshError
                ))
            } else {
                emit(Result.Success(comments))
            }
        }
    }.flowOn(ioDispatcher)

    override suspend fun addLocalComment(comment: Comment): Result<Unit> {
        return try {
            val entity = comment.copy(isLocal = true).toEntity().copy(id = 0) // id = 0 → auto-generate
            localDataSource.insert(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Failed to save comment",
                throwable = e
            )
        }
    }

    /**
     * Fetches comments from the API and replaces remote-only cache.
     * Preserves local user-created comments by only deleting isLocal = false entries.
     */
    private suspend fun tryRefreshFromApi(postId: Int): Throwable? {
        return try {
            val remoteComments = remoteDataSource.getCommentsByPostId(postId)
            localDataSource.deleteRemoteCommentsByPostId(postId)
            localDataSource.insertAll(remoteComments)
            null
        } catch (e: Exception) {
            e
        }
    }
}
