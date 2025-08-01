package com.coredumped.project.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context.findActivity()

    Box(modifier = Modifier.fillMaxSize()) {
        // Add the same background image as HomeScreen for consistency and visual appeal
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null, // No desc needed for background
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay a subtle translucent scrim for better text/button readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), // Increased padding for a more open feel
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Language / ভাষা নির্বাচন করুন / भाषा चुनें",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White, // White for visibility on dark scrim
                modifier = Modifier.padding(bottom = 32.dp) // Space below title
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Playful, bigger English button with rounded corners and custom colors
                Button(
                    onClick = {
                        saveLanguagePreference(context, "en")
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true } // Remove settings from back stack
                            launchSingleTop = true // Avoid multiple copies of home
                        }
                        activity?.recreate() // Recreates activity to apply language
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)), // Rounded for playful look
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50), // Green for English (vibrant, child-friendly)
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "English",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Playful, bigger Bengali button with rounded corners and custom colors
                Button(
                    onClick = {
                        saveLanguagePreference(context, "bn")
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true } // Remove settings from back stack
                            launchSingleTop = true // Avoid multiple copies of home
                        }
                        activity?.recreate() // Recreates activity to apply language and route to home
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)), // Rounded for playful look
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107), // Yellow for Bengali (warm, inviting)
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "বাংলা",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Playful, bigger Hindi button with rounded corners and custom colors
                Button(
                    onClick = {
                        saveLanguagePreference(context, "hi")
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true } // Remove settings from back stack
                            launchSingleTop = true // Avoid multiple copies of home
                        }
                        activity?.recreate() // Recreates activity to apply language and route to home
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)), // Rounded for playful look
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3), // Blue for Hindi
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "हिंदी",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/* For settings, move to separate file later */
private fun saveLanguagePreference(context: Context, lang: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("preferred_language", lang).commit() // Use commit() for synchronous save
}