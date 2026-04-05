package com.github.arlidhr.posts_offline_app.modules.posts.data.datasource

import com.github.arlidhr.posts_offline_app.modules.posts.data.dao.PostDao
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Room-backed implementation of [PostLocalDataSource].
 *
 * Delegates all operations to [PostDao]. This thin wrapper exists to maintain
 * the DataSource abstraction, allowing the DAO implementation to be swapped
 * without modifying the repository layer.
 */
class PostLocalDataSourceImpl @Inject constructor(
    private val postDao: PostDao
) : PostLocalDataSource {

    override fun getAllPosts(): Flow<List<PostEntity>> =
        postDao.getAllPosts()

    override suspend fun getPostById(id: Int): PostEntity? =
        postDao.getPostById(id)

    override fun searchPosts(query: String): Flow<List<PostEntity>> =
        postDao.searchPosts(query)

    override suspend fun refreshPosts(posts: List<PostEntity>) =
        postDao.refreshPosts(posts)
}

