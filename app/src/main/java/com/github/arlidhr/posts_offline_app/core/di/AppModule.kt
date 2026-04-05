package com.github.arlidhr.posts_offline_app.core.di

import com.github.arlidhr.posts_offline_app.networking.ConnectivityObserver
import com.github.arlidhr.posts_offline_app.networking.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifiers for coroutine dispatchers.
 * Allows injecting specific dispatchers for testability.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * Hilt module providing app-level dependencies:
 * Coroutine Dispatchers.
 *
 * NOTE: AppDatabase provider will be added in feature/posts-module
 * when Room @Database annotation is activated with entities.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}

/**
 * Hilt module for interface-to-implementation bindings.
 * Separated from [AppModule] because @Binds requires an abstract class.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        impl: NetworkConnectivityObserver
    ): ConnectivityObserver
}
