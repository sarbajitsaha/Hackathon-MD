package com.coredumped.project

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.activity.BrushScreen
import com.coredumped.project.activity.HandWashScreen
import com.coredumped.project.calm.CalmVideoScreen
import com.coredumped.project.activity.IdCardScreen
import com.coredumped.project.activity.SocializeScreen
import com.coredumped.project.calm.FluidSimulationScreen
import com.coredumped.project.calm.PopBubbleScreen
import com.coredumped.project.ui.LearningScreen
import com.coredumped.project.ui.CalmAudioScreen
import com.coredumped.project.ui.CalmScreen
import com.coredumped.project.ui.DailyActivityScreen
import com.coredumped.project.learning.FlashCardsAlphabetsScreen
import com.coredumped.project.learning.FlashCardsFruitsScreen
import com.coredumped.project.learning.FlashCardsAnimalsScreen
import com.coredumped.project.learning.FlashCardsStationaryScreen
import com.coredumped.project.learning.FlashCardsVehiclesScreen
import com.coredumped.project.learning.FlashCardsDaysScreen
import com.coredumped.project.learning.FlashCardsNumbersScreen
import com.coredumped.project.ui.HomeScreen
import com.coredumped.project.ui.SettingsScreen
import com.coredumped.project.ui.IQScreen
import com.coredumped.project.iq.IQTestScreen
import com.coredumped.project.iq.Maths0Screen
import com.coredumped.project.iq.Maths1Screen
import com.coredumped.project.learning.FlashCardsVehiclesScreenPreview

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    val animatorScale = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController, prefs = prefs)
        }
        animatedComposable(
            route = "settings",
            scale = animatorScale
        ) {
            SettingsScreen(navController)
        }
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
            route = "brush",
            scale = animatorScale
        ) {
            BrushScreen(navController = navController)
        }
        animatedComposable(
            route = "handwash",
            scale = animatorScale
        ) {
            HandWashScreen(navController = navController)
        }
        animatedComposable(
            route = "socialize",
            scale = animatorScale
        ) {
            SocializeScreen(navController = navController)
        }
        animatedComposable(
            route = "idcard",
            scale = animatorScale
        ) {
            IdCardScreen(navController = navController)
        }
        animatedComposable(
            route = "learning",
            scale = animatorScale
        ) {
            LearningScreen(navController)
        }
        animatedComposable(
            route = "flashcards_alphabets",
            scale = animatorScale
        ) {
            FlashCardsAlphabetsScreen(navController)
        }
        animatedComposable(
            route = "flashcards_days",
            scale = animatorScale
        ) {
            FlashCardsDaysScreen(navController)
        }
        animatedComposable(
            route = "flashcards_numbers",
            scale = animatorScale
        ) {
            FlashCardsNumbersScreen(navController)
        }
        animatedComposable(
            route = "flashcards_fruits",
            scale = animatorScale
        ) {
            FlashCardsFruitsScreen(navController)
        }
        animatedComposable(
            route = "flashcards_animals",
            scale = animatorScale
        ) {
            FlashCardsAnimalsScreen(navController)
        }
        animatedComposable(
            route = "flashcards_stationary",
            scale = animatorScale
        ) {
            FlashCardsStationaryScreen(navController)
        }
        animatedComposable(
            route = "flashcards_vehicles",
            scale = animatorScale
        ) {
            FlashCardsVehiclesScreen(navController)
        }
        animatedComposable(
            route = "maths_0",
            scale = animatorScale
        ) {
            Maths0Screen(navController)
        }
        animatedComposable(
            route = "maths_1",
            scale = animatorScale
        ) {
            Maths1Screen(navController)
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
            IQScreen(navController)
        }
        animatedComposable(
            route = "iq_test",
            scale = animatorScale
        ) {
            IQTestScreen(navController)
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
        animatedComposable(
            route = "calm_audio",
            scale = animatorScale
        ) {
            CalmAudioScreen(navController = navController)
        }
        animatedComposable(
            route = "calm_video",
            scale = animatorScale
        ) {
            CalmVideoScreen(navController = navController)
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
                null
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = (300 * scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween((150 * scale).toInt().coerceAtLeast(50)))
            }
        },
        exitTransition = {
            if (scale == 0f) {
                null
            } else {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(
                        durationMillis = (300 * scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween((150 * scale).toInt().coerceAtLeast(50)))
            }
        },
        popEnterTransition = {
            if (scale == 0f) {
                null
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(
                        durationMillis = (300 * scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeIn(animationSpec = tween((150 * scale).toInt().coerceAtLeast(50)))
            }
        },
        popExitTransition = {
            if (scale == 0f) {
                null
            } else {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(
                        durationMillis = (300 * scale).toInt().coerceAtLeast(100),
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeOut(animationSpec = tween((150 * scale).toInt().coerceAtLeast(50)))
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

@Composable
fun SplashScreen(navController: NavController, prefs: SharedPreferences) {
    LaunchedEffect(Unit) {
        val isLanguageSet = prefs.contains("preferred_language")
        val destination = if (isLanguageSet) "home" else "settings"

        navController.navigate(destination) {
            popUpTo("splash") {
                inclusive = true
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(50.dp)
        )
    }
}