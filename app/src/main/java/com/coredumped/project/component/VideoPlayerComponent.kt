package com.coredumped.project.component // Or your chosen package for components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

private const val TAG = "VideoPlayerCmp" // Shortened tag for the component

@Composable
fun VideoPlayerComponent(
    modifier: Modifier = Modifier,
    videoResourceId: Int,
    onPlayerReady: (ExoPlayer) -> Unit,
    onRelease: () -> Unit,
    autoPlay: Boolean = true // Added parameter to control autoPlay
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val uriString = "android.resource://${context.packageName}/$videoResourceId"
            Log.d(TAG, "Preparing video from resource URI: $uriString for resource ID: $videoResourceId")
            val mediaItem = MediaItem.fromUri(uriString)
            setMediaItem(mediaItem)
            prepare()
            // playWhenReady will be set based on the autoPlay parameter via onPlayerReady
        }
    }

    LaunchedEffect(exoPlayer, autoPlay) { // React to autoPlay changes if needed, though usually set once
        onPlayerReady(exoPlayer) // Always call onPlayerReady
        if (autoPlay) {
            // If autoPlay is true, the caller (BrushScreen/HandWashScreen)
            // will typically set player.playWhenReady = true in its onPlayerReady callback.
            // This component itself won't directly start playback unless explicitly told.
            // However, the onPlayerReady callback gives the caller the control.
            // For screens that auto-play, they will set it there.
            // For screens that don't (like the original BrushScreen with a play button),
            // they would set it upon button click.
            // The `autoPlay` parameter here is more of an indicator for how the component
            // is intended to be used by the caller.
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                // Consider making 'useController' a parameter if you need to toggle it
                // useController = false
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing and releasing player for resource ID: $videoResourceId")
            exoPlayer.release()
            onRelease()
        }
    }
}

// Optional: Context.getActivity() extension
fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}