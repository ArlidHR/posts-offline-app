package com.github.arlidhr.posts_offline_app.modules.comments.data.datasource

import com.github.arlidhr.posts_offline_app.modules.comments.data.dao.CommentDao
import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-backed implementation of [CommentLocalDataSource].
 * Delegates all operations to [CommentDao].
 */
class CommentLocalDataSourceImpl @Inject constructor(
    private val commentDao: CommentDao
) : CommentLocalDataSource {

    override fun getCommentsByPostId(postId: Int): Flow<List<CommentEntity>> =
        commentDao.getCommentsByPostId(postId)

    override suspend fun insertAll(comments: List<CommentEntity>) =
        commentDao.insertAll(comments)

    override suspend fun insert(comment: CommentEntity) =
        commentDao.insert(comment)

    override suspend fun deleteRemoteCommentsByPostId(postId: Int) =
        commentDao.deleteRemoteCommentsByPostId(postId)
}

