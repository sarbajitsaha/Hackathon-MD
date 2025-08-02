package com.coredumped.project.learning

import android.net.Uri
import android.util.Log
// import androidx.annotation.DrawableRes // Not needed anymore
import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
// import androidx.compose.foundation.Image // Not needed anymore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
// import androidx.compose.ui.layout.ContentScale // Not needed anymore
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
// import androidx.compose.ui.res.painterResource // Not needed anymore
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coredumped.project.R // Make sure this import is correct for your project
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private const val MEDIA_PLAYER_DAYS_TAG = "FlashCardDaysAudioPlayer"
private const val AUDIO_LOOP_DELAY_MS_DAYS = 4500L

// Background colors for the screen
val flashcardDaysScreenBackgroundColors = listOf(
    Color(0xFFE3F2FD), // Light Blue
    Color(0xFFFFF9C4), // Light Yellow
    Color(0xFFE8F5E9), // Light Green
    Color(0xFFFCE4EC), // Light Pink
    Color(0xFFF9FBE7), // Lighter Yellow
    Color(0xFFEDE7F6), // Light Purple
    Color(0xFFFFF3E0)  // Light Orange
)

// Vibrant text colors for the days' names
val dayNameTextColors = listOf(
    Color(0xFFD32F2F), // Red
    Color(0xFFC2185B), // Pink
    Color(0xFF7B1FA2), // Purple
    Color(0xFF512DA8), // Deep Purple
    Color(0xFF303F9F), // Indigo
    Color(0xFF1976D2), // Blue
    Color(0xFF0288D1)  // Light Blue
)

data class DayFlashCardItem(
    val nameKey: String, // e.g., "sunday"
    @RawRes val audioResId: Int
    // No imageResId needed anymore
)

// Define the days of the week
val daysFlashCards = listOf(
    DayFlashCardItem("sunday", R.raw.sunday_en_us_1),
    DayFlashCardItem("monday", R.raw.monday_en_us_1),
    DayFlashCardItem("tuesday", R.raw.tuesday_en_in_1),
    DayFlashCardItem("wednesday", R.raw.wednesday_en_in_1),
    DayFlashCardItem("thursday", R.raw.thursday_en_in_1),
    DayFlashCardItem("friday", R.raw.friday_en_in_1),
    DayFlashCardItem("saturday", R.raw.saturday_en_in_1)
)

@Composable
fun FlashCardsDaysScreen(navController: NavController) {
    var currentCardIndex by remember { mutableIntStateOf(0) }
    val currentCard = if (daysFlashCards.isNotEmpty()) daysFlashCards[currentCardIndex] else null

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var audioLoopJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(context) {
        Log.d(MEDIA_PLAYER_DAYS_TAG, "Initializing ExoPlayer for Days")
        val player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(MEDIA_PLAYER_DAYS_TAG, "onIsPlayingChanged: $isPlaying")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && audioLoopJob?.isActive == false) {
                        Log.d(MEDIA_PLAYER_DAYS_TAG, "Playback ended, and loop job is not active.")
                    }
                }
            })
        }
        exoPlayer = player
        onDispose {
            Log.d(MEDIA_PLAYER_DAYS_TAG, "Releasing ExoPlayer for Days")
            audioLoopJob?.cancel()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    audioLoopJob?.cancel()
                    exoPlayer?.pause()
                    Log.d(MEDIA_PLAYER_DAYS_TAG, "Screen paused, pausing player and loop.")
                }
                Lifecycle.Event.ON_RESUME -> Log.d(MEDIA_PLAYER_DAYS_TAG, "Screen resumed.")
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(MEDIA_PLAYER_DAYS_TAG, "Screen destroyed.")
                    audioLoopJob?.cancel()
                    exoPlayer?.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun playDayAudio(audioResId: Int) {
        exoPlayer?.let { player ->
            if (player.isPlaying) player.stop()
            val uri = Uri.parse("android.resource://${context.packageName}/$audioResId")
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            Log.d(MEDIA_PLAYER_DAYS_TAG, "Playing audio: resId $audioResId")
        }
    }

    LaunchedEffect(currentCardIndex, exoPlayer, currentCard) {
        audioLoopJob?.cancel()
        exoPlayer?.stop()
        if (exoPlayer != null && currentCard != null) {
            audioLoopJob = launch {
                while (true) {
                    if (exoPlayer?.isPlaying == false) {
                        playDayAudio(currentCard.audioResId)
                    }
                    val currentAudioDuration = if (exoPlayer?.isPlaying == true) exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L else 0L
                    delay(AUDIO_LOOP_DELAY_MS_DAYS + currentAudioDuration + 500L)
                }
            }
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val textToDisplay = currentCard?.let { stringResource(id = getDayLabelRes(it.nameKey)) } ?: "..."

    val dynamicBackgroundColor by animateColorAsState(
        targetValue = if (daysFlashCards.isNotEmpty()) flashcardDaysScreenBackgroundColors[currentCardIndex % flashcardDaysScreenBackgroundColors.size] else Color.LightGray,
        animationSpec = tween(durationMillis = 500), label = "days_bg_color_anim"
    )

    // Determine the text color based on the current card index
    val currentTextColor = if (daysFlashCards.isNotEmpty()) dayNameTextColors[currentCardIndex % dayNameTextColors.size] else MaterialTheme.colorScheme.onSurface


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackgroundColor)
    ) {
        if (currentCard == null) {
            Text("Loading days...", Modifier.align(Alignment.Center))
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(min(64f, screenWidth * 0.12f).dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))))
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(min(32f, screenWidth * 0.06f).dp))
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Row: Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(min(64f, screenWidth * 0.12f).dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1976D2), Color(0xFF2196F3), Color(0xFF64B5F6)) // Day-themed gradient
                            )
                        )
                        .clickable {
                            audioLoopJob?.cancel()
                            exoPlayer?.stop()
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(min(32f, screenWidth * 0.06f).dp)
                    )
                }
            }

            // Flashcard Content (Text Only)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f), // Takes available vertical space
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = textToDisplay,
                    fontSize = 80.sp, // Made text larger as it's the main focus
                    fontWeight = FontWeight.Bold,
                    color = currentTextColor, // Use the dynamic text color
                    style = MaterialTheme.typography.displayLarge.copy( // Using displayLarge
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.25f),
                            offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                            blurRadius = 6f
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp) // Add some padding around the text
                )
            }

            // Spacer before bottom navigation buttons
            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Buttons (Previous/Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navButtonSize = 72.dp
                val navIconSize = 48.dp

                Box(
                    modifier = Modifier
                        .size(navButtonSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = daysFlashCards.isNotEmpty()) {
                            if (currentCardIndex > 0) currentCardIndex--
                            else currentCardIndex = daysFlashCards.size - 1
                            audioLoopJob?.cancel()
                            exoPlayer?.stop()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBack, "Previous Day", Modifier.size(navIconSize), MaterialTheme.colorScheme.onPrimary)
                }

                Box(
                    modifier = Modifier
                        .size(navButtonSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = daysFlashCards.isNotEmpty()) {
                            if (currentCardIndex < daysFlashCards.size - 1) currentCardIndex++
                            else currentCardIndex = 0
                            audioLoopJob?.cancel()
                            exoPlayer?.stop()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowForward, "Next Day", Modifier.size(navIconSize), MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

private fun getDayLabelRes(textKey: String): Int {
    return when (textKey.lowercase()) {
        "sunday" -> R.string.flashcards_sunday
        "monday" -> R.string.flashcards_monday
        "tuesday" -> R.string.flashcards_tuesday
        "wednesday" -> R.string.flashcards_wednesday
        "thursday" -> R.string.flashcards_thursday
        "friday" -> R.string.flashcards_friday
        "saturday" -> R.string.flashcards_saturday
        else -> R.string.test
    }
}

@Preview(showBackground = true)
@Composable
fun FlashCardsDaysScreenPreview() {
    MaterialTheme {
        FlashCardsDaysScreen(navController = rememberNavController())
    }
}

