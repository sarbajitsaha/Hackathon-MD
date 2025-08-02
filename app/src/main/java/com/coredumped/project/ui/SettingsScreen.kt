package com.coredumped.project.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.BackgroundMusic
import com.coredumped.project.R

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context.findActivity()
    var isMuted by remember { mutableStateOf(BackgroundMusic.isMuted(context)) }

    // Check if there's a screen to go back to.
    val canPop = navController.previousBackStackEntry != null

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Adjust padding to push content down if back button is visible
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    bottom = 32.dp,
                    top = if (canPop) 96.dp else 32.dp
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Language / ভাষা নির্বাচন করুন / भाषा चुनें",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        saveLanguagePreference(context, "en")
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true }
                            launchSingleTop = true
                        }
                        activity?.recreate()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "English",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        saveLanguagePreference(context, "bn")
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true }
                            launchSingleTop = true
                        }
                        activity?.recreate()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC107),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "বাংলা",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        saveLanguagePreference(context, "hi")
                        navController.navigate("home") {
                            popUpTo("settings") { inclusive = true }
                            launchSingleTop = true
                        }
                        activity?.recreate()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
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
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    BackgroundMusic.toggleMute(context)
                    isMuted = !isMuted
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(70.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMuted) Color(0xFFF44336) else Color(0xFF795548),
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MusicOff else Icons.Default.MusicNote,
                        contentDescription = "Mute/Unmute Music"
                    )
                    Text(text = if (isMuted) stringResource(id=R.string.unmute) else stringResource(id=R.string.mute), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Only show Back Button if we can pop the stack
        if (canPop) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(64.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF9500),
                                Color(0xFFFF2D55),
                                Color(0xFF5856D6)
                            )
                        )
                    )
                    .clickable { navController.popBackStack() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

private fun saveLanguagePreference(context: Context, lang: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("preferred_language", lang).apply()
}