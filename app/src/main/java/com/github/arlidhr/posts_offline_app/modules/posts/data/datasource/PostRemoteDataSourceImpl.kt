package com.github.arlidhr.posts_offline_app.modules.posts.data.datasource

import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity
import com.github.arlidhr.posts_offline_app.modules.posts.data.service.PostApiService
import javax.inject.Inject

/**
 * Retrofit-backed implementation of [PostRemoteDataSource].
 *
 * Delegates all operations to [PostApiService]. This thin wrapper exists to maintain
 * the DataSource abstraction, allowing the HTTP client to be swapped
 * without modifying the repository layer.
 */
class PostRemoteDataSourceImpl @Inject constructor(
    private val apiService: PostApiService
) : PostRemoteDataSource {

    override suspend fun getPosts(): List<PostEntity> =
        apiService.getPosts()

    override suspend fun getPostById(id: Int): PostEntity =
        apiService.getPostById(id)
}

