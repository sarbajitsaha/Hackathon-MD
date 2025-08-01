package com.coredumped.project

import android.media.MediaPlayer
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun BackgroundMusic(@RawRes musicResId: Int) {
    val context = LocalContext.current

    // This effect will start when the composable enters the composition
    // and clean up when it leaves.
    DisposableEffect(musicResId) {
        val player = MediaPlayer.create(context, musicResId).apply {
            isLooping = true // Set the music to loop
            start()        // Start playing
        }

        // onDispose is called when the composable is removed from the screen
        onDispose {
            player.stop()
            player.release()
        }
    }
}