package com.coredumped.project.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import android.util.Log
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

private const val TAG = "CalmAudioScreen"

/* Copyright free music flute - https://www.youtube.com/watch?v=5TStK8S_zFQ
 * Nature sounds from pixabay
 */

@Composable
fun CalmAudioScreen(navController: NavController) {
    Log.d(TAG, "Rendering CalmAudioScreen")

    val context = LocalContext.current
    var currentPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentSoundName by remember { mutableStateOf("") }
    var volume by remember { mutableFloatStateOf(1.0f) }

    // Clean up player on dispose
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing audio player")
            currentPlayer?.release()
            currentPlayer = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = stringResource(R.string.calm_background_desc),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Nature Sounds
            item(span = { GridItemSpan(4) }, key = "nature_header") {
                CategoryHeader(text = stringResource(R.string.category_nature_sounds))
            }

            val natureSounds = listOf(
                SoundData(R.drawable.calm_rain, R.string.sound_rain, "nature_rain"),
                SoundData(R.drawable.calm_bird, R.string.sound_birds, "nature_birds")
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
                            currentPlayer?.setVolume(volume, volume)
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true
                        }
                    }
                )
            }

            // Music Sounds
            item(span = { GridItemSpan(4) }, key = "music_header") {
                CategoryHeader(text = stringResource(R.string.category_music_sounds))
            }

            val musicSounds = listOf(
                SoundData(R.drawable.calm_flute, R.string.sound_flute, "music_flute"),
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
                            currentPlayer?.setVolume(volume, volume)
                            currentSoundName = soundName
                            currentPlayer?.start()
                            isPlaying = true
                            currentPlayer?.isLooping = true
                        }
                    }
                )
            }
        }

        // Back button
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
                .clickable {
                    Log.d(TAG, "Back button clicked")
                    navController.popBackStack()
                }
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

        // Media controls if playing
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
                Slider(
                    value = volume,
                    onValueChange = { newVolume ->
                        volume = newVolume
                        currentPlayer?.setVolume(newVolume, newVolume)
                        Log.d(TAG, "Volume adjusted to $newVolume")
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.5f)  // Slightly transparent for the inactive part to distinguish it
                    )
                )
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(onClick = {
                        if (isPlaying) {
                            Log.d(TAG, "Pausing audio")
                            currentPlayer?.pause()
                        } else {
                            Log.d(TAG, "Resuming audio")
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
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    IconButton(onClick = {
                        Log.d(TAG, "Stopping audio")
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
                            modifier = Modifier.size(64.dp)
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
    val key: String
)

@Composable
fun CategoryHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.65f))
        )

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            color = Color.Black,
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .size(110.dp)
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

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.7f))
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

private fun playSound(
    context: Context,
    imageResId: Int,
    onPlayerReady: (MediaPlayer, String) -> Unit
) {
    val soundName = context.resources.getResourceEntryName(imageResId)
    val rawId = context.resources.getIdentifier(soundName, "raw", context.packageName)
    if (rawId != 0) {
        Log.d(TAG, "Playing sound: $soundName")
        val player = MediaPlayer.create(context, rawId)
        player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        player.setOnCompletionListener {
            it.release()
        }
        onPlayerReady(player, soundName.replace("_", " ").capitalize())
    } else {
        Log.e(TAG, "Missing raw resource for $soundName")
    }
}