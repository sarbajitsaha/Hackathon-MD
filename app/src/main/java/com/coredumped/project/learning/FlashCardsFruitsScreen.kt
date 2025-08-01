package com.coredumped.project.learning

import android.net.Uri
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
// import androidx.compose.material.icons.filled.VolumeUp // Can be re-added for manual play
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.coredumped.project.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private const val MEDIA_PLAYER_TAG = "FlashCardAudioPlayer"
private const val AUDIO_LOOP_DELAY_MS = 3000L // 3 seconds delay between audio plays

// Background colors
val flashcardFruitsBackgroundColors = listOf(
    Color(0xFFFFF3E0), Color(0xFFE3F2FD), Color(0xFFE8F5E9),
    Color(0xFFFCE4EC), Color(0xFFF9FBE7), Color(0xFFEDE7F6)
)

// Data class with audio resource ID
data class FruitFlashCardItem(
    val nameKey: String, // Key for string resource (e.g., "apple")
    @DrawableRes val imageResId: Int,
    @RawRes val audioResId: Int // Resource ID from res/raw
)

// Sample data - update with your actual raw audio files
val fruitFlashCards = listOf(
    FruitFlashCardItem("apple", R.drawable.apple, R.raw.apple_en_us_2), // e.g., res/raw/apple_sound.mp3
    FruitFlashCardItem("banana", R.drawable.banana, R.raw.banana_en_us_2),
    FruitFlashCardItem("mango", R.drawable.mango, R.raw.mango_en_us_2),
    FruitFlashCardItem("orange", R.drawable.orange, R.raw.orange_en_us_2),
    FruitFlashCardItem("potato", R.drawable.potato, R.raw.potato_en_us_1),
    FruitFlashCardItem("carrot", R.drawable.carrot, R.raw.carrot_en_us_2)
)

@Composable
fun FlashCardsFruitsScreen(navController: NavController) {
    var currentCardIndex by remember { mutableIntStateOf(0) }
    val currentCard = fruitFlashCards[currentCardIndex]

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var audioLoopJob by remember { mutableStateOf<Job?>(null) }
    var isPlayingAudio by remember { mutableStateOf(false) }


    // Initialize ExoPlayer
    DisposableEffect(context) {
        Log.d(MEDIA_PLAYER_TAG, "Initializing ExoPlayer")
        val player = ExoPlayer.Builder(context).build().apply {
            // Set repeat mode for single item looping IF the delay logic wasn't present.
            // With delay, we'll manually seek and play.
            // repeatMode = Player.REPEAT_MODE_ONE // Not strictly needed with our manual loop

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    isPlayingAudio = isPlaying
                    Log.d(MEDIA_PLAYER_TAG, "onIsPlayingChanged: $isPlaying")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && audioLoopJob?.isActive == false)
                    {
                        // This case handles when the sound finishes naturally
                        // and our delay loop isn't about to restart it.
                        // For our timed loop, this might not be strictly necessary
                        // as the loop itself will restart it.
                        Log.d(MEDIA_PLAYER_TAG, "Playback ended, not part of active loop restart.")
                    }
                }
            })
        }
        exoPlayer = player

        onDispose {
            Log.d(MEDIA_PLAYER_TAG, "Releasing ExoPlayer")
            audioLoopJob?.cancel()
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    // Lifecycle observer to pause/resume player and loop
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    audioLoopJob?.cancel() // Stop the loop
                    exoPlayer?.pause()     // Pause player
                    Log.d(MEDIA_PLAYER_TAG, "Screen paused, pausing player and loop.")
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Loop will be restarted by LaunchedEffect if card/player is ready
                    Log.d(MEDIA_PLAYER_TAG, "Screen resumed.")
                }
                Lifecycle.Event.ON_DESTROY -> { // Should be covered by onDispose of player
                    Log.d(MEDIA_PLAYER_TAG, "Screen destroyed.")
                    audioLoopJob?.cancel()
                    exoPlayer?.release() // Ensure release
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Function to prepare and play audio
    fun playAudio(audioResId: Int) {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop() // Stop current playback before starting new
            }
            val uri = Uri.parse("android.resource://${context.packageName}/$audioResId")
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            Log.d(MEDIA_PLAYER_TAG, "Playing audio: resId $audioResId")
        }
    }

    // Audio Playback Loop Logic
    LaunchedEffect(currentCardIndex, exoPlayer) {
        audioLoopJob?.cancel() // Cancel previous loop
        exoPlayer?.stop()      // Stop any currently playing audio immediately

        if (exoPlayer != null) {
            audioLoopJob = launch {
                while (true) {
                    // Only play if player is not already playing (e.g. from a natural end)
                    // and our loop intends to play it now.
                    if (exoPlayer?.isPlaying == false) {
                        playAudio(currentCard.audioResId)
                    } else if (exoPlayer?.isPlaying == true) {
                        Log.d(MEDIA_PLAYER_TAG, "Audio already playing, loop will wait for next cycle or completion.")
                    }
                    delay(AUDIO_LOOP_DELAY_MS + (if (exoPlayer?.isPlaying == true) exoPlayer?.duration?.coerceAtLeast(0L) ?: 0L else 0L) + 500L)
                    // The delay logic:
                    // 1. Base delay (AUDIO_LOOP_DELAY_MS)
                    // 2. If it's currently playing, add its duration to wait for it to finish.
                    // 3. Add a small buffer (500ms).
                    // This ensures we wait for the audio to complete if it started, then wait the additional delay.

                    // Alternative simpler delay if you always want to interrupt and restart:
                    // playAudio(currentCard.audioResId) // Play immediately
                    // delay(AUDIO_LOOP_DELAY_MS) // Then wait
                }
            }
        }
    }


    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val textToDisplay = stringResource(id = getFruitLabelRes(currentCard.nameKey))

    val dynamicBackgroundColor by animateColorAsState(
        targetValue = flashcardFruitsBackgroundColors[currentCardIndex % flashcardFruitsBackgroundColors.size],
        animationSpec = tween(durationMillis = 500), label = "fruits_bg_color_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicBackgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {



            // Flashcard Content
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = currentCard.imageResId),
                    contentDescription = textToDisplay,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp)) // Adjust spacer if needed

                Text(
                    text = textToDisplay,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(8.dp)) // Adjust spacer if needed
            }

            // Bottom Navigation Buttons (Previous/Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navButtonSize = 72.dp
                val navIconSize = 48.dp

                // Previous Button
                Box(
                    modifier = Modifier
                        .size(navButtonSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = fruitFlashCards.isNotEmpty()) {
                            if (currentCardIndex > 0) currentCardIndex--
                            else currentCardIndex = fruitFlashCards.size - 1
                            audioLoopJob?.cancel() // Stop current loop, LaunchedEffect will restart
                            exoPlayer?.stop()      // Stop current audio immediately
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowBack, "Previous Card", Modifier.size(navIconSize), MaterialTheme.colorScheme.onPrimary)
                }

                // Next Button
                Box(
                    modifier = Modifier
                        .size(navButtonSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = fruitFlashCards.isNotEmpty()) {
                            if (currentCardIndex < fruitFlashCards.size - 1) currentCardIndex++
                            else currentCardIndex = 0
                            audioLoopJob?.cancel() // Stop current loop, LaunchedEffect will restart
                            exoPlayer?.stop()      // Stop current audio immediately
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ArrowForward, "Next Card", Modifier.size(navIconSize), MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        // Styled Back Button
        val backButtonSize = min(64f, screenWidth * 0.12f).dp
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(backButtonSize)
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
                    audioLoopJob?.cancel()
                    exoPlayer?.stop()
                    navController.popBackStack()
                }
                .align(Alignment.TopStart),
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
}

// Ensure string resources exist (e.g., in values/strings.xml)
private fun getFruitLabelRes(textKey: String): Int {
    return when (textKey.lowercase()) {
        "apple" -> R.string.flashcards_apple
        "banana" -> R.string.flashcards_banana
        "mango" -> R.string.flashcards_mango
        "orange" -> R.string.flashcards_orange
        "potato" -> R.string.flashcards_potato
        "carrot" -> R.string.flashcards_carrot
        else -> R.string.test // Fallback
    }
}

@Preview(showBackground = true)
@Composable
fun FlashCardsFruitsScreenPreview() {
    MaterialTheme {
        // For preview, ExoPlayer won't initialize, but UI can be seen
        FlashCardsFruitsScreen(navController = rememberNavController())
    }
}
