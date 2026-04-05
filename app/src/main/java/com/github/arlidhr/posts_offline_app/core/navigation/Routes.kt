package com.github.arlidhr.posts_offline_app.core.navigation

/**
 * Type-safe navigation routes for the application.
 *
 * Each screen is represented as a [data object] with its route string.
 * Routes with arguments provide a [createRoute] helper to build the full path.
 *
 * Usage:
 *   navController.navigate(Routes.PostsList.route)
 *   navController.navigate(Routes.PostComments.createRoute(postId = 1))
 *
 * Adding a new screen: create a new [data object] inside this sealed class.
 */
sealed class Routes(val route: String) {

    /** Posts list screen — entry point of the app */
    data object PostsList : Routes("posts_list")

    /** Comments screen for a specific post — receives postId as argument */
    data object PostComments : Routes("post_comments/{postId}") {
        const val ARG_POST_ID = "postId"
        fun createRoute(postId: Int): String = "post_comments/$postId"
    }
}
