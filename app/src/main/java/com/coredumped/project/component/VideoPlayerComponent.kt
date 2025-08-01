package com.coredumped.project.component

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private const val TAG = "VideoPlayerComponent"

/**
 * A reusable Compose component for playing videos using ExoPlayer.
 * It handles setup, playback configuration via callback, and cleanup.
 *
 * @param modifier Modifier for the view.
 * @param videoResourceId The raw resource ID of the video to play.
 * @param onPlayerReady Callback to configure the player (e.g., set repeat mode or start playback).
 * @param onRelease Callback when the player is released (optional, for any cleanup).
 * @param autoPlay If true, automatically starts playback when ready (defaults to true).
 */
@Composable
fun VideoPlayerComponent(
    modifier: Modifier = Modifier,
    videoResourceId: Int,
    onPlayerReady: (ExoPlayer) -> Unit = {},
    onRelease: () -> Unit = {},
    autoPlay: Boolean = true
) {
    val context = LocalContext.current

    // Remember the ExoPlayer instance to persist across recompositions
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uri = "android.resource://${context.packageName}/$videoResourceId"
            Log.d(TAG, "Loading video from URI: $uri (resource ID: $videoResourceId)")
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = autoPlay  // Start playback automatically if enabled
        }
    }

    // Call the onPlayerReady callback once the player is set up
    LaunchedEffect(Unit) {
        onPlayerReady(exoPlayer)
    }

    // Embed the PlayerView in Compose
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false  // Hide all built-in playback controls
            }
        }
    )

    // Release resources when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Releasing player for resource ID: $videoResourceId")
            exoPlayer.release()
            onRelease()
        }
    }
}