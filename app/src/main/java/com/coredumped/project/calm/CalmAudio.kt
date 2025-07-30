package com.coredumped.project.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coredumped.project.R

@Composable
fun CalmAudioScreen(navController: NavController) {
    val context = LocalContext.current
    var currentPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentSoundName by remember { mutableStateOf("") }

    // Release player when screen is disposed (e.g., navigation back)
    DisposableEffect(Unit) {
        onDispose {
            currentPlayer?.release()
            currentPlayer = null
        }
    }

    // Container Box to hold the background, grid, back button, and media controls
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image (use a calm, soothing image; placeholder ID assumed)
        Image(
            painter = painterResource(id = R.drawable.homescreen), // Replace with your calm background resource
            contentDescription = "Calm Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Grid layout for categories and sound items
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 4 columns for landscape mode, spacious for big icons
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Increased padding for better spacing in landscape
            verticalArrangement = Arrangement.spacedBy(16.dp), // Larger vertical spacing for touch-friendly layout
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            // Nature Sounds Header
            item(span = { GridItemSpan(4) }) {
                CategoryHeader(text = "Nature Sounds")
            }

            // 4 Nature Sound Items (big icons, placeholders)
            items(4) { index ->
                SoundItem(
                    imageResId = when (index) {
                        0 -> R.drawable.calm_rain // Placeholder: rain icon
                        1 -> R.drawable.calm_waves // Placeholder: ocean waves icon
                        2 -> R.drawable.calm // Placeholder: forest birds icon
                        else -> R.drawable.calm // Placeholder: wind chimes icon
                    },
                    onClick = { imageResId ->
                        playSound(context, imageResId) { newPlayer, soundName ->
                            // Stop and release current player if exists
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            // Set new player and start
                            currentPlayer = newPlayer
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true // Enable looping
                        }
                    }
                )
            }

            // Mindfulness Sounds Header
            item(span = { GridItemSpan(4) }) {
                CategoryHeader(text = "Mindfulness Sounds")
            }

            // 4 Mindfulness Sound Items (big icons, placeholders)
            items(4) { index ->
                SoundItem(
                    imageResId = when (index) {
                        0 -> R.drawable.calm // Placeholder: breathing exercise icon
                        1 -> R.drawable.calm // Placeholder: cloud floating icon
                        2 -> R.drawable.calm // Placeholder: body relaxation icon
                        else -> R.drawable.calm // Placeholder: soft whisper icon
                    },
                    onClick = { imageResId ->
                        playSound(context, imageResId) { newPlayer, soundName ->
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            currentPlayer = newPlayer
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true // Enable looping
                        }
                    }
                )
            }

            // Music Sounds Header
            item(span = { GridItemSpan(4) }) {
                CategoryHeader(text = "Music Sounds")
            }

            // 4 Music Sound Items (big icons, placeholders)
            items(4) { index ->
                SoundItem(
                    imageResId = when (index) {
                        0 -> R.drawable.calm // Placeholder: piano icon
                        1 -> R.drawable.calm // Placeholder: harp icon
                        2 -> R.drawable.calm // Placeholder: flute icon
                        else -> R.drawable.calm // Placeholder: ambient melody icon
                    },
                    onClick = { imageResId ->
                        playSound(context, imageResId) { newPlayer, soundName ->
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            currentPlayer = newPlayer
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true // Enable looping
                        }
                    }
                )
            }
        }

        // Colorful back button in the top left corner
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9500),  // Orange
                            Color(0xFFFF2D55),  // Pink
                            Color(0xFF5856D6)   // Purple
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

        // Media controls at the bottom (visible only if playing)
        if (currentPlayer != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = currentSoundName,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        if (isPlaying) {
                            currentPlayer?.pause()
                        } else {
                            currentPlayer?.start()
                        }
                        isPlaying = !isPlaying
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp) // Larger icon
                        )
                    }
                    IconButton(onClick = {
                        currentPlayer?.stop()
                        currentPlayer?.release()
                        currentPlayer = null
                        isPlaying = false
                        currentSoundName = ""
                    }) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp) // Larger icon
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp), // Large, readable font
            color = Color.White, // High contrast on background
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SoundItem(
    imageResId: Int,
    onClick: (Int) -> Unit // Now passes imageResId to parent for playback
) {
    // Box to center the card
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Card for the sound icon (larger for big icons)
        Card(
            modifier = Modifier
                .clickable { onClick(imageResId) } // Pass imageResId to handle playback
                .size(150.dp), // Fixed size to match image, no extra padding
            shape = RoundedCornerShape(12.dp), // Rounded corners for the card
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)), // Subtle translucent background
            elevation = CardDefaults.cardElevation(1.dp) // Slight elevation for depth
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Sound Icon",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// Helper function to load MediaPlayer for a sound
private fun playSound(
    context: Context,
    imageResId: Int,
    onPlayerReady: (MediaPlayer, String) -> Unit
) {
    val soundName = context.resources.getResourceEntryName(imageResId) // e.g., "calm_rain"
    val rawId = context.resources.getIdentifier(soundName, "raw", context.packageName)
    if (rawId != 0) { // Check if resource exists
        val player = MediaPlayer.create(context, rawId)
        player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK) // Keep audio playing when screen off
        player.setOnCompletionListener {
            it.release() // Auto-release on completion (though looping prevents this)
        }
        onPlayerReady(player, soundName.capitalize())
    } else {
        // Handle missing MP3 (e.g., log error or show toast)
    }
}