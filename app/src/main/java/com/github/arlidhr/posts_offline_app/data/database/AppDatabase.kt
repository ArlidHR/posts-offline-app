package com.github.arlidhr.posts_offline_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.arlidhr.posts_offline_app.core.utils.Constants
import com.github.arlidhr.posts_offline_app.modules.posts.data.dao.PostDao
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity

/**
 * Room Database — Single source of truth for local persistence.
 *
 * Currently includes:
 * - [PostEntity] / [PostDao] — Posts table
 *
 * Will be extended with:
 * - CommentEntity / CommentDao — in feature/comments-module
 *
 * Version history:
 * - v1: Initial schema with posts table
 */
@Database(
    entities = [PostEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                Constants.DATABASE_NAME
            ).build()
        }
    }
}
