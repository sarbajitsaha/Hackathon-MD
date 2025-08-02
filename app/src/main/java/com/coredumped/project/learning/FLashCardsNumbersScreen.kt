package com.coredumped.project.learning

import android.net.Uri
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.coredumped.project.R // Make sure this import is correct
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private const val MEDIA_PLAYER_NUMBERS_TAG = "FlashCardNumbersAudioPlayer"
private const val AUDIO_LOOP_DELAY_MS_NUMBERS = 4500L

// Background colors for the screen
val flashcardNumbersScreenBackgroundColors = listOf(
    Color(0xFFFDEFD5), Color(0xFFE5F8E2), Color(0xFFDDF4F8),
    Color(0xFFF6E3F9), Color(0xFFFFEBEB), Color(0xFFE0F7FA),
    Color(0xFFFFF9C4), Color(0xFFFBE9E7), Color(0xFFE8EAF6),
    Color(0xFFE0F2F1)
)

// Vibrant text colors for the numbers
val numberNameTextColors = listOf(
    Color(0xFFE53935), // Red
    Color(0xFFD81B60), // Pink
    Color(0xFF8E24AA), // Purple
    Color(0xFF5E35B1), // Deep Purple
    Color(0xFF3949AB), // Indigo
    Color(0xFF1E88E5), // Blue
    Color(0xFF039BE5), // Light Blue
    Color(0xFF00ACC1), // Cyan
    Color(0xFF00897B), // Teal
    Color(0xFF43A047)  // Green
)

data class NumberFlashCardItem(
    val numberValue: Int, // The actual number, e.g., 1, 2, 3
    val displayText: String, // Text to display, e.g., "1", "2"
    @RawRes val audioResId: Int,
    val stringResourceKey: String // e.g., "number_one" for R.string.flashcards_number_one
)

// Define the numbers (e.g., 1 to 10)
val numbersFlashCards = listOf(
    NumberFlashCardItem(1, "1", R.raw.one, "number_one"),
    NumberFlashCardItem(2, "2", R.raw.two, "number_two"),
    NumberFlashCardItem(3, "3", R.raw.three, "number_three"),
    NumberFlashCardItem(4, "4", R.raw.four, "number_four"),
    NumberFlashCardItem(5, "5", R.raw.five, "number_five"),
    NumberFlashCardItem(6, "6", R.raw.six, "number_six"),
    NumberFlashCardItem(7, "7", R.raw.seven, "number_seven"),
    NumberFlashCardItem(8, "8", R.raw.eight, "number_eight"),
    NumberFlashCardItem(9, "9", R.raw.nine, "number_nine"),
    NumberFlashCardItem(10, "10", R.raw.ten, "number_ten")
    // Add more numbers as needed, up to 20 or more.
    // Ensure you have corresponding audio and string resources.
)

@Composable
fun FlashCardsNumbersScreen(navController: NavController) {
    var currentCardIndex by remember { mutableIntStateOf(0) }
    val currentCard = if (numbersFlashCards.isNotEmpty()) numbersFlashCards[currentCardIndex] else null

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var audioLoopJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(context) {
        Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Initializing ExoPlayer for Numbers")
        val player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d(MEDIA_PLAYER_NUMBERS_TAG, "onIsPlayingChanged: $isPlaying")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && audioLoopJob?.isActive == false) {
                        Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Playback ended, and loop job is not active.")
                    }
                }
            })
        }
        exoPlayer = player
        onDispose {
            Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Releasing ExoPlayer for Numbers")
            audioLoopJob?.cancel()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    audioLoopJob?.cancel(); exoPlayer?.pause()
                    Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Screen paused.")
                }
                Lifecycle.Event.ON_RESUME -> Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Screen resumed.")
                Lifecycle.Event.ON_DESTROY -> {
                    audioLoopJob?.cancel(); exoPlayer?.release()
                    Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Screen destroyed.")
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun playNumberAudio(audioResId: Int) {
        exoPlayer?.let { player ->
            if (player.isPlaying) player.stop()
            val uri = Uri.parse("android.resource://${context.packageName}/$audioResId")
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            Log.d(MEDIA_PLAYER_NUMBERS_TAG, "Playing audio: resId $audioResId")
        }
    }

    LaunchedEffect(currentCardIndex, exoPlayer, currentCard) {
        audioLoopJob?.cancel()
        exoPlayer?.stop()
        if (exoPlayer != null && currentCard != null) {
            audioLoopJob = launch {
                while (true) {
                    if (exoPlayer?.isPlaying == false) {
                        playNumberAudio(currentCard.audioResId)
                    }
                    val currentAudioDuration = if (exoPlayer?.isPlaying == true) exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L else 0L
                    delay(AUDIO_LOOP_DELAY_MS_NUMBERS + currentAudioDuration + 500L)
                }
            }
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    // val textToDisplay = currentCard?.let { stringResource(id = getNumberLabelRes(it.stringResourceKey)) } ?: "..."
    // For numbers, we typically display the digit itself. The stringResourceKey is for the audio label or accessibility.
    val textToDisplay = currentCard?.displayText ?: "..."


    val dynamicBackgroundColor by animateColorAsState(
        targetValue = if (numbersFlashCards.isNotEmpty()) flashcardNumbersScreenBackgroundColors[currentCardIndex % flashcardNumbersScreenBackgroundColors.size] else Color.LightGray,
        animationSpec = tween(durationMillis = 500), label = "numbers_bg_color_anim"
    )

    val currentTextColor = if (numbersFlashCards.isNotEmpty()) numberNameTextColors[currentCardIndex % numberNameTextColors.size] else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackgroundColor)
    ) {
        if (currentCard == null) {
            Text("Loading numbers...", Modifier.align(Alignment.Center))
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(min(64f, screenWidth * 0.12f).dp)
                    .shadow(4.dp, CircleShape).clip(CircleShape)
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
                        .shadow(4.dp, CircleShape).clip(CircleShape)
                        .background(brush = Brush.linearGradient(colors = listOf(Color(0xFF00796B), Color(0xFF009688), Color(0xFF4DB6AC)))) // Number-themed gradient
                        .clickable {
                            audioLoopJob?.cancel(); exoPlayer?.stop()
                            navController.popBackStack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(min(32f, screenWidth * 0.06f).dp))
                }
            }

            // Flashcard Content (Text Only)
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = textToDisplay, // Displaying the number digit directly
                    fontSize = 120.sp, // Even larger for single/double digit numbers
                    fontWeight = FontWeight.Bold,
                    color = currentTextColor,
                    style = MaterialTheme.typography.displayLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.25f),
                            offset = androidx.compose.ui.geometry.Offset(5f, 5f),
                            blurRadius = 7f
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navButtonSize = 72.dp; val navIconSize = 48.dp
                Box(
                    modifier = Modifier.size(navButtonSize).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = numbersFlashCards.isNotEmpty()) {
                            if (currentCardIndex > 0) currentCardIndex-- else currentCardIndex = numbersFlashCards.size - 1
                            audioLoopJob?.cancel(); exoPlayer?.stop()
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.ArrowBack, "Previous Number", Modifier.size(navIconSize), MaterialTheme.colorScheme.onPrimary) }

                Box(
                    modifier = Modifier.size(navButtonSize).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = numbersFlashCards.isNotEmpty()) {
                            if (currentCardIndex < numbersFlashCards.size - 1) currentCardIndex++ else currentCardIndex = 0
                            audioLoopJob?.cancel(); exoPlayer?.stop()
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.ArrowForward, "Next Number", Modifier.size(navIconSize), MaterialTheme.colorScheme.onPrimary) }
            }
        }
    }
}

// Optional: String resource helper if you need to fetch full names like "One", "Two"
// For now, we are displaying digits directly from NumberFlashCardItem.displayText
/*
private fun getNumberLabelRes(textKey: String): Int {
    return when (textKey.lowercase()) {
        "number_one" -> R.string.flashcards_number_one
        "number_two" -> R.string.flashcards_number_two
        "number_three" -> R.string.flashcards_number_three
        "number_four" -> R.string.flashcards_number_four
        "number_five" -> R.string.flashcards_number_five
        "number_six" -> R.string.flashcards_number_six
        "number_seven" -> R.string.flashcards_number_seven
        "number_eight" -> R.string.flashcards_number_eight
        "number_nine" -> R.string.flashcards_number_nine
        "number_ten" -> R.string.flashcards_number_ten
        // Add more numbers as needed
        else -> R.string.test
    }
}
*/

@Preview(showBackground = true)
@Composable
fun FlashCardsNumbersScreenPreview() {
    MaterialTheme {
        FlashCardsNumbersScreen(navController = rememberNavController())
    }
}
