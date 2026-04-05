package com.github.arlidhr.posts_offline_app.modules.posts.di

import com.github.arlidhr.posts_offline_app.data.database.AppDatabase
import com.github.arlidhr.posts_offline_app.modules.posts.data.dao.PostDao
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostLocalDataSource
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostLocalDataSourceImpl
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostRemoteDataSource
import com.github.arlidhr.posts_offline_app.modules.posts.data.datasource.PostRemoteDataSourceImpl
import com.github.arlidhr.posts_offline_app.modules.posts.data.repository.PostRepositoryImpl
import com.github.arlidhr.posts_offline_app.modules.posts.data.service.PostApiService
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt Module for Posts feature.
 * Binds PostRepository interface to PostRepositoryImpl.
 * Provides UseCases and DataSources.
 */
@Module
@InstallIn(SingletonComponent::class)
object PostsProviderModule {

    @Provides
    @Singleton
    fun providePostApiService(retrofit: Retrofit): PostApiService =
        retrofit.create(PostApiService::class.java)

    @Provides
    fun providePostDao(database: AppDatabase): PostDao =
        database.postDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PostsBindsModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindPostLocalDataSource(
        impl: PostLocalDataSourceImpl
    ): PostLocalDataSource

    @Binds
    @Singleton
    abstract fun bindPostRemoteDataSource(
        impl: PostRemoteDataSourceImpl
    ): PostRemoteDataSource
}
