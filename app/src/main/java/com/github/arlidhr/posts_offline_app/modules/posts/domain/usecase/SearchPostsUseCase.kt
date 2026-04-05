package com.github.arlidhr.posts_offline_app.modules.posts.domain.usecase

import com.github.arlidhr.posts_offline_app.core.error.Result
import com.github.arlidhr.posts_offline_app.modules.posts.domain.model.Post
import com.github.arlidhr.posts_offline_app.modules.posts.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Use case: Search posts by title (name) or ID.
 *
 * Encapsulates search logic with input validation:
 * - Trims whitespace from the query
 * - Returns empty success if query is blank (shows all posts scenario is handled by [GetPostsUseCase])
 *
 * Fulfills functional requirement:
 * "En la pantalla donde se listan las publicaciones debo tener la opción
 *  de poder realizar una búsqueda por Nombre o por ID."
 *
 * Search is performed against the local database, making it fully offline-capable.
 */
class SearchPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    operator fun invoke(query: String): Flow<Result<List<Post>>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            return flowOf(Result.Success(emptyList()))
        }
        return repository.searchPosts(trimmedQuery)
    }
}
