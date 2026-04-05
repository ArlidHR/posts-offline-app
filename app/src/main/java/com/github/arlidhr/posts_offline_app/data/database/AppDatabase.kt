package com.github.arlidhr.posts_offline_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.arlidhr.posts_offline_app.core.utils.Constants
import com.github.arlidhr.posts_offline_app.modules.comments.data.dao.CommentDao
import com.github.arlidhr.posts_offline_app.modules.comments.data.entity.CommentEntity
import com.github.arlidhr.posts_offline_app.modules.posts.data.dao.PostDao
import com.github.arlidhr.posts_offline_app.modules.posts.data.entity.PostEntity

/**
 * Room Database — Single source of truth for local persistence.
 *
 * Current schema (v2):
 * - [PostEntity] / [PostDao] — posts table
 * - [CommentEntity] / [CommentDao] — comments table with FK → posts
 *
 * Version history:
 * - v1: posts table
 * - v2: comments table added (fallbackToDestructiveMigration for development)
 */
@Database(
    entities = [PostEntity::class, CommentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                Constants.DATABASE_NAME
            )
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}
