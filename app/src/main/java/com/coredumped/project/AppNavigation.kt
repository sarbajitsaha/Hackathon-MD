package com.coredumped.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.ui.CalmScreen
import com.coredumped.project.calm.FluidSimulationScreen
import com.coredumped.project.calm.PopBubbleScreen
import com.coredumped.project.ui.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("daily_activity") {
            PlaceholderScreen("Daily Activity Screen")
        }
        composable("learning") {
            PlaceholderScreen("Learning Screen")
        }
        composable("calm") {
            CalmScreen(navController)
        }
        composable("iq") {
            PlaceholderScreen("IQ Screen")
        }
        composable("fluid_simulation") {
            FluidSimulationScreen(navController = navController)
        }
        //composable("calming_audio") { CalmingAudioScreen() }
        //composable("calming_video") { CalmingVideoScreen() }
        composable("pop_bubble") { PopBubbleScreen(navController = navController) }
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