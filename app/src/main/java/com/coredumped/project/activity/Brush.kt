package com.coredumped.project.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.coredumped.project.R
// Import the shared component
import com.coredumped.project.component.VideoPlayerComponent // ** UPDATE THIS IMPORT **

// Logging tag
private const val TAG = "BrushVideoScreen"

// Resource ID for your specific video
private val VIDEO_RESOURCE_ID = R.raw.brush_video


@Composable
fun BrushScreen(navController: NavController) {
    var hasVideoStarted by remember { mutableStateOf(false) }
    var exoPlayerInstance by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(Unit) {
        Log.d(TAG, "BrushScreen opened, will attempt to play video immediately.")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VideoPlayerComponent(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            videoResourceId = VIDEO_RESOURCE_ID,
            onPlayerReady = { player ->
                if (!hasVideoStarted) {
                    Log.d(TAG, "BrushScreen: ExoPlayer is ready. Starting playback.")
                    player.playWhenReady = true // Auto-start playback
                    exoPlayerInstance = player
                    hasVideoStarted = true
                }
            },
            onRelease = {
                Log.d(TAG, "BrushScreen: VideoPlayerComponent was released.")
                hasVideoStarted = false
                exoPlayerInstance = null
            },
            autoPlay = true // Explicitly indicating intent, though logic is in onPlayerReady
        )

        // Simple Back/Close Button
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(56.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable {
                    Log.d(TAG, "Back button clicked. Navigating back.")
                    exoPlayerInstance?.stop()
                    navController.popBackStack()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "BrushScreen disposing. Stopping player.")
            exoPlayerInstance?.stop()
        }
    }
}
