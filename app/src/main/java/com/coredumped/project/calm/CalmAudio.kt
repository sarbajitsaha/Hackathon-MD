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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = stringResource(R.string.calm_background_desc),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Grid layout for categories and sound items
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 4 columns for landscape mode
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Reduced padding to improve performance
            verticalArrangement = Arrangement.spacedBy(12.dp), // Slightly reduced spacing
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Extra padding at bottom for media controls
        ) {
            // Nature Sounds Header
            item(span = { GridItemSpan(4) }, key = "nature_header") {
                CategoryHeader(text = stringResource(R.string.category_nature_sounds))
            }

            // 4 Nature Sound Items
            val natureSounds = listOf(
                SoundData(R.drawable.calm_rain, R.string.sound_rain, "nature_rain"),
                SoundData(R.drawable.calm_waves, R.string.sound_waves, "nature_waves"),
                SoundData(R.drawable.calm_forest, R.string.sound_forest, "nature_forest"),
                SoundData(R.drawable.calm, R.string.sound_birds, "nature_birds")
            )

            items(natureSounds, key = { it.key }) { soundData ->
                SoundItem(
                    imageResId = soundData.imageResId,
                    textResId = soundData.textResId,
                    onClick = { imageResId ->
                        playSound(context, imageResId) { newPlayer, soundName ->
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            currentPlayer = newPlayer
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true
                        }
                    }
                )
            }

            /*
            // Mindfulness Sounds Header
            item(span = { GridItemSpan(4) }, key = "mindfulness_header") {
                CategoryHeader(text = stringResource(R.string.category_mindfulness_sounds))
            }

            // 4 Mindfulness Sound Items
            val mindfulnessSounds = listOf(
                SoundData(R.drawable.calm, R.string.sound_breathing, "mind_breathing"),
                SoundData(R.drawable.calm, R.string.sound_cloud, "mind_cloud"),
                SoundData(R.drawable.calm, R.string.sound_relaxation, "mind_relax"),
                SoundData(R.drawable.calm, R.string.sound_whisper, "mind_whisper")
            )

            items(mindfulnessSounds, key = { it.key }) { soundData ->
                SoundItem(
                    imageResId = soundData.imageResId,
                    textResId = soundData.textResId,
                    onClick = { imageResId ->
                        playSound(context, imageResId) { newPlayer, soundName ->
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            currentPlayer = newPlayer
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true
                        }
                    }
                )
            }
             */

            // Music Sounds Header
            item(span = { GridItemSpan(4) }, key = "music_header") {
                CategoryHeader(text = stringResource(R.string.category_music_sounds))
            }

            // 4 Music Sound Items
            val musicSounds = listOf(
                SoundData(R.drawable.calm, R.string.sound_piano, "music_piano"),
                SoundData(R.drawable.calm, R.string.sound_harp, "music_harp"),
                SoundData(R.drawable.calm, R.string.sound_flute, "music_flute"),
                SoundData(R.drawable.calm, R.string.sound_ambient, "music_ambient")
            )

            items(musicSounds, key = { it.key }) { soundData ->
                SoundItem(
                    imageResId = soundData.imageResId,
                    textResId = soundData.textResId,
                    onClick = { imageResId ->
                        playSound(context, imageResId) { newPlayer, soundName ->
                            currentPlayer?.stop()
                            currentPlayer?.release()
                            currentPlayer = newPlayer
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true
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
                contentDescription = stringResource(R.string.back_button),
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
                            contentDescription = if (isPlaying)
                                stringResource(R.string.pause_button)
                            else stringResource(R.string.play_button),
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
                            contentDescription = stringResource(R.string.stop_button),
                            tint = Color.White,
                            modifier = Modifier.size(48.dp) // Larger icon
                        )
                    }
                }
            }
        }
    }
}

data class SoundData(
    val imageResId: Int,
    val textResId: Int,
    val key: String // Added for better recycling performance
)

@Composable
fun CategoryHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp) // Fixed height for better performance
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Translucent background for better text readability
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f) // Not full width to look better
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.65f)) // Translucent white background
        )

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp), // Slightly smaller for better performance
            color = Color.Black, // Dark text on light translucent background
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SoundItem(
    imageResId: Int,
    textResId: Int,
    onClick: (Int) -> Unit
) {
    // Using Box instead of Column for better performance
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card for the sound icon
            Card(
                modifier = Modifier
                    .size(110.dp) // Slightly smaller for better performance
                    .clickable { onClick(imageResId) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = stringResource(id = textResId),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Text with translucent background
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.7f)) // Translucent background
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(id = textResId),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
        onPlayerReady(player, soundName.replace("_", " ").capitalize())
    } else {
        // Handle missing MP3 (e.g., log error or show toast)
    }
}