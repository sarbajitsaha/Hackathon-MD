package com.coredumped.project

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.ui.DailyActivityScreen
import com.coredumped.project.ui.CalmScreen
import com.coredumped.project.calm.FluidSimulationScreen
import com.coredumped.project.calm.PopBubbleScreen
import com.coredumped.project.ui.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val animatorScale = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
    }

    NavHost(navController = navController, startDestination = "home") {
        animatedComposable(
            route = "home",
            scale = animatorScale
        ) {
            HomeScreen(navController)
        }
        animatedComposable(
            route = "daily_activity",
            scale = animatorScale
        ) {
            DailyActivityScreen(navController)
        }
        animatedComposable(
            route = "learning",
            scale = animatorScale
        ) {
            PlaceholderScreen("Learning Screen")
        }
        animatedComposable(
            route = "calm",
            scale = animatorScale
        ) {
            CalmScreen(navController)
        }
        animatedComposable(
            route = "iq",
            scale = animatorScale
        ) {
            PlaceholderScreen("IQ Screen")
        }
        animatedComposable(
            route = "fluid_simulation",
            scale = animatorScale
        ) {
            FluidSimulationScreen(navController = navController)
        }
        animatedComposable(
            route = "pop_bubble",
            scale = animatorScale
        ) {
            PopBubbleScreen(navController = navController)
        }
    }
}

fun NavGraphBuilder.animatedComposable(
    route: String,
    scale: Float,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            if (scale == 0f) {
                null // Skip animations if animator scale is 0
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth }, // Start from right
                    animationSpec = tween(
                        durationMillis = (300 / scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween((150 / scale).toInt().coerceAtLeast(50)))
            }
        },
        exitTransition = {
            if (scale == 0f) {
                null // Skip animations
            } else {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth }, // Exit to left
                    animationSpec = tween(
                        durationMillis = (300 / scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween((150 / scale).toInt().coerceAtLeast(50)))
            }
        },
        popEnterTransition = {
            if (scale == 0f) {
                null // Skip animations
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth }, // Start from left
                    animationSpec = tween(
                        durationMillis = (300 / scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween((150 / scale).toInt().coerceAtLeast(50)))
            }
        },
        popExitTransition = {
            if (scale == 0f) {
                null // Skip animations
            } else {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth }, // Exit to right
                    animationSpec = tween(
                        durationMillis = (300 / scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween((150 / scale).toInt().coerceAtLeast(50)))
            }
        }
    ) { backStackEntry ->
        content(backStackEntry)
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title - Coming Soon!")
    }
}