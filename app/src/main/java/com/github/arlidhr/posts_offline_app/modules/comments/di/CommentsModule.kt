package com.github.arlidhr.posts_offline_app.modules.comments.di

import com.github.arlidhr.posts_offline_app.data.database.AppDatabase
import com.github.arlidhr.posts_offline_app.modules.comments.data.dao.CommentDao
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentLocalDataSource
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentLocalDataSourceImpl
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentRemoteDataSource
import com.github.arlidhr.posts_offline_app.modules.comments.data.datasource.CommentRemoteDataSourceImpl
import com.github.arlidhr.posts_offline_app.modules.comments.data.repository.CommentRepositoryImpl
import com.github.arlidhr.posts_offline_app.modules.comments.data.service.CommentApiService
import com.github.arlidhr.posts_offline_app.modules.comments.domain.repository.CommentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt Module for Comments feature.
 * Binds CommentRepository interface to CommentRepositoryImpl.
 * Provides UseCases and DataSources.
 */
@Module
@InstallIn(SingletonComponent::class)
object CommentsProviderModule {

    @Provides
    @Singleton
    fun provideCommentApiService(retrofit: Retrofit): CommentApiService =
        retrofit.create(CommentApiService::class.java)

    @Provides
    fun provideCommentDao(database: AppDatabase): CommentDao =
        database.commentDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CommentsBindsModule {

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: CommentRepositoryImpl
    ): CommentRepository

    @Binds
    @Singleton
    abstract fun bindCommentLocalDataSource(
        impl: CommentLocalDataSourceImpl
    ): CommentLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCommentRemoteDataSource(
        impl: CommentRemoteDataSourceImpl
    ): CommentRemoteDataSource
}
