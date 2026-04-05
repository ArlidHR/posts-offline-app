package com.github.arlidhr.posts_offline_app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.github.arlidhr.posts_offline_app.modules.comments.presentation.view.CommentsScreen
import com.github.arlidhr.posts_offline_app.modules.posts.presentation.view.PostsListScreen

/**
 * Application navigation graph using Jetpack Navigation Compose.
 *
 * Defines all screen destinations and transitions.
 *
 * @param navController The navigation controller managing the back stack.
 * @param modifier Optional modifier for the NavHost container.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.PostsList.route,
        modifier = modifier
    ) {
        // Posts list screen
        composable(route = Routes.PostsList.route) {
            PostsListScreen(
                onPostClick = { postId ->
                    navController.navigate(Routes.PostComments.createRoute(postId))
                }
            )
        }

        // Post comments screen
        composable(
            route = Routes.PostComments.route,
            arguments = listOf(
                navArgument(Routes.PostComments.ARG_POST_ID) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt(Routes.PostComments.ARG_POST_ID) ?: 0
            CommentsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
