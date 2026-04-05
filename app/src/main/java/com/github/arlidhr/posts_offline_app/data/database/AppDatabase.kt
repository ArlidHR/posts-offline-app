package com.github.arlidhr.posts_offline_app.data.database

import android.content.Context
import androidx.room.RoomDatabase
import com.github.arlidhr.posts_offline_app.core.utils.Constants

/**
 * Room Database - Single source of truth for local persistence.
 *
 * NOTE: @Database annotation and entities will be added in feature/posts-module
 * when PostEntity is created. Room requires at least one entity to compile.
 *
 * This file serves as the placeholder that will be completed with:
 * - @Database(entities = [PostEntity::class, CommentEntity::class], version = 1)
 * - abstract fun postDao(): PostDao
 * - abstract fun commentDao(): CommentDao
 */
abstract class AppDatabase : RoomDatabase() {

    companion object {
        fun create(context: Context): AppDatabase {
            return androidx.room.Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                Constants.DATABASE_NAME
            ).build()
        }
    }
}
