package com.coredumped.project

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * A singleton object to manage a single instance of MediaPlayer for background music.
 * This ensures music plays continuously across different screens and can be paused or
 * resumed from anywhere in the app.
 */
object BackgroundMusic {
    private var player: MediaPlayer? = null
    private var isPlayerInitialized = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fadeInJob: Job? = null
    private const val MAX_VOLUME = 0.4f // The target volume
    private const val FADE_DURATION_MS = 2000 // seconds for the fade

    /**
     * Starts playing the background music. If music is already playing, it does nothing.
     *
     * @param context The application context.
     * @param musicResId The raw resource ID of the music file.
     */
    fun start(context: Context, @RawRes musicResId: Int) {
        if (player == null) {
            player = MediaPlayer.create(context, musicResId).apply {
                isLooping = true
            }
            player?.setOnErrorListener { _, _, _ ->
                isPlayerInitialized = false
                player?.release()
                player = null
                true
            }
            player?.setOnPreparedListener {
                isPlayerInitialized = true
                performFadeIn() // Start with a fade-in once prepared
            }
        } else {
            // If already initialized but not playing, resume with a fade-in
            resume()
        }
    }

    /**
     * Pauses the music and cancels any ongoing fade-in.
     */
    fun pause() {
        fadeInJob?.cancel() // Important: stop the fade if it's in progress
        if (player?.isPlaying == true) {
            player?.pause()
        }
    }

    /**
     * Resumes the music with a fade-in effect if it was paused.
     */
    fun resume() {
        if (player?.isPlaying == false && isPlayerInitialized) {
            fadeInJob?.cancel() // Cancel any previous job
            performFadeIn()
        }
    }

    /**
     * The core fade-in logic. Starts the player at volume 0 and gradually increases it.
     */
    private fun performFadeIn() {
        if (!isPlayerInitialized || player == null) return

        // Launch a coroutine to handle the volume change over time
        fadeInJob = scope.launch {
            player?.setVolume(0f, 0f) // Start at zero volume
            player?.start()

            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() < startTime + FADE_DURATION_MS) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val currentVolume = (elapsedTime.toFloat() / FADE_DURATION_MS) * MAX_VOLUME

                // Check if player is still valid before setting volume
                if (player != null) {
                    player?.setVolume(currentVolume, currentVolume)
                } else {
                    break // Player was released, stop the coroutine
                }
                delay(50) // Update volume every 50ms
            }
            // Ensure the final volume is set correctly
            player?.setVolume(MAX_VOLUME, MAX_VOLUME)
        }
    }


    /**
     * Stops and releases the MediaPlayer instance to free up resources.
     */
    fun release() {
        fadeInJob?.cancel()
        player?.stop()
        player?.release()
        player = null
        isPlayerInitialized = false
    }
}