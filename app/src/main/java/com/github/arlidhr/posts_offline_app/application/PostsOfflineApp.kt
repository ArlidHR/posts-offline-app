package com.github.arlidhr.posts_offline_app.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class - Entry point for Hilt dependency injection.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation.
 */
@HiltAndroidApp
class PostsOfflineApp : Application()
