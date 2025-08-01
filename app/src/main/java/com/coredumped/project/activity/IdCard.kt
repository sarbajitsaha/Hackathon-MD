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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.coredumped.project.R
// Import the shared component
import com.coredumped.project.component.VideoPlayerComponent // ** UPDATE THIS IMPORT **

// Logging tag
private const val TAG = "IdCardVideoScreen"

// Resource ID for your specific video
private val VIDEO_RESOURCE_ID = R.raw.idcard_video


@Composable
fun IdCardScreen(navController: NavController) {
    var hasVideoStarted by remember { mutableStateOf(false) }
    var exoPlayerInstance by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(Unit) {
        Log.d(TAG, "IdCardScreen opened, will attempt to play video immediately.")
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
                    Log.d(TAG, "IdCardScreen: ExoPlayer is ready. Starting playback.")
                    player.playWhenReady = true // Auto-start playback
                    player.repeatMode = Player.REPEAT_MODE_ONE // Loop the video
                    exoPlayerInstance = player
                    hasVideoStarted = true
                }
            },
            onRelease = {
                Log.d(TAG, "IdCardScreen: VideoPlayerComponent was released.")
                hasVideoStarted = false
                exoPlayerInstance = null
            },
            autoPlay = true // Explicitly indicating intent for immediate playback
        )

        // Back button (always visible)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF9500), Color(0xFFFF2D55), Color(0xFF5856D6))
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
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "IdCardScreen disposing. Stopping player.")
            exoPlayerInstance?.stop()
        }
    }
}
