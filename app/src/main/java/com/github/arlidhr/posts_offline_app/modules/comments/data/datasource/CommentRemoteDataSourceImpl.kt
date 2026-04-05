package com.github.arlidhr.posts_offline_app.modules.comments.data.datasource

import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import com.github.arlidhr.posts_offline_app.modules.comments.data.service.CommentApiService
import javax.inject.Inject

/**
 * Retrofit-backed implementation of [CommentRemoteDataSource].
 * Delegates all operations to [CommentApiService].
 */
class CommentRemoteDataSourceImpl @Inject constructor(
    private val apiService: CommentApiService
) : CommentRemoteDataSource {

    override suspend fun getCommentsByPostId(postId: Int): List<CommentEntity> =
        apiService.getCommentsByPostId(postId)
}

