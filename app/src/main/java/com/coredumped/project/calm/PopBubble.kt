package com.coredumped.project.calm

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun PopBubbleScreen(navController: NavController) {
    val context = LocalContext.current
    val popSound = remember { MediaPlayer.create(context, R.raw.bubble_pop) }
    val bubbles = remember { mutableStateListOf<Bubble>() }
    val particles = remember { mutableStateListOf<BubbleParticle>() }
    val coroutineScope = rememberCoroutineScope()
    val spawnDelay = remember { mutableStateOf(1000L) }
    val lastPopTime = remember { mutableStateOf(0L) }
    val popRate = remember { mutableStateOf(0f) }
    val updateTrigger = remember { mutableStateOf(0) }
    val showTutorial = remember { mutableStateOf(true) }

    // Game loop for updating bubbles and particles
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            val currentTime = System.currentTimeMillis()
            bubbles.forEach { bubble ->
                if (bubble.isPopping) {
                    // Continue pop animation
                } else {
                    bubble.y -= bubble.speed
                }
            }
            particles.forEach { particle ->
                particle.x += particle.vx
                particle.y += particle.vy
                particle.alpha -= 0.02f // Fade out over ~50 frames
            }
            // Remove off-screen/popped bubbles and faded particles
            bubbles.removeAll { it.y + it.radius < 0 || (it.isPopping && it.scale.value <= 0f) }
            particles.removeAll { it.alpha <= 0f }
            // Update pop rate
            if (currentTime - lastPopTime.value < 1000L) {
                popRate.value = 1f / ((currentTime - lastPopTime.value) / 1000f)
            } else {
                popRate.value = 0f
            }
            // Adjust spawn delay
            spawnDelay.value = (1000f - (popRate.value * 100f)).toLong().coerceIn(200L, 1000L)
            // Trigger recomposition
            updateTrigger.value++
        }
    }

    // Spawn bubbles periodically with overlap check
    LaunchedEffect(Unit) {
        while (true) {
            delay(spawnDelay.value)
            val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
            val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
            val y = screenHeight + 50f // Start off bottom; adjusted min radius
            var attempts = 0
            var spawned = false
            while (attempts < 10 && !spawned) {
                attempts++
                val radius = Random.nextFloat() * 50 + 100 // 100-150
                val x = Random.nextFloat() * (screenWidth - 2 * radius) + radius
                val speed = Random.nextFloat() * 5 + 2 // 2-7
                val candidate = Bubble(x, y, radius, speed)
                if (!willCollideWithAny(candidate, bubbles)) {
                    bubbles.add(candidate)
                    spawned = true
                }
            }
            // If no good position after 10 tries, skip this spawn to avoid forcing overlaps
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.pop_bubble_background),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        bubbles.forEach { bubble ->
                            if (!bubble.isPopping) {
                                val dist = sqrt((offset.x - bubble.x).pow(2) + (offset.y - bubble.y).pow(2))
                                if (dist <= bubble.radius) {
                                    // Pop the bubble
                                    coroutineScope.launch {
                                        bubble.isPopping = true
                                        bubble.scale.animateTo(0f, animationSpec = tween(300))
                                    }
                                    // Spawn pop particles
                                    repeat(Random.nextInt(8, 12)) {
                                        val angle = Random.nextFloat() * 2 * PI.toFloat()
                                        val particleSpeed = Random.nextFloat() * 10 + 5
                                        val vx = cos(angle) * particleSpeed
                                        val vy = sin(angle) * particleSpeed
                                        val particleRadius = Random.nextFloat() * 10 + 5
                                        particles.add(BubbleParticle(bubble.x, bubble.y, vx, vy, particleRadius))
                                    }
                                    // Restart sound if already playing
                                    if (popSound.isPlaying) {
                                        popSound.pause()
                                        popSound.seekTo(0)
                                    }
                                    popSound.start()
                                    lastPopTime.value = System.currentTimeMillis()
                                    return@forEach
                                }
                            }
                        }
                    }
                }
        ) {
            updateTrigger.value // Trigger redraw
            bubbles.forEach { bubble ->
                val alpha = if (bubble.isPopping) bubble.scale.value else 1f
                val scaledRadius = bubble.radius * bubble.scale.value
                val center = Offset(bubble.x, bubble.y)
                // Base semi-transparent bubble
                drawCircle(
                    color = Color.Blue.copy(alpha = alpha * 0.3f),
                    radius = scaledRadius,
                    center = center
                )
                // White highlight for shine (offset top-left)
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.8f),
                    radius = scaledRadius / 4,
                    center = center + Offset(-scaledRadius / 3, -scaledRadius / 3)
                )
            }
            particles.forEach { particle ->
                // Draw particles with similar style but smaller
                drawCircle(
                    color = Color.Blue.copy(alpha = particle.alpha * 0.3f),
                    radius = particle.radius,
                    center = Offset(particle.x, particle.y)
                )
            }
        }

        if (showTutorial.value) {
            TutorialFinger(bubbles = bubbles, onComplete = { showTutorial.value = false }, coroutineScope = coroutineScope, popSound = popSound, particles = particles)
        }
    }
}

@Composable
fun TutorialFinger(
    bubbles: List<Bubble>,
    onComplete: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    popSound: MediaPlayer,
    particles: MutableList<BubbleParticle>
) {
    if (bubbles.isEmpty()) return

    val firstBubble = bubbles.first()
    val density = LocalDensity.current
    val fingerSize = 50.dp
    val fingerSizePx = with(density) { fingerSize.toPx() }

    // Start finger off-screen to the right
    val fingerX = remember { Animatable(bubbles.first().x + 200f) } // Start to the right of the bubble
    val fingerY = remember { Animatable(bubbles.first().y) }
    val fingerScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Animate finger moving left to the bubble position
        launch {
            fingerX.animateTo(firstBubble.x, animationSpec = tween(1000)) // Move over 1 second
        }
        launch {
            fingerY.animateTo(firstBubble.y, animationSpec = tween(1000))
        }

        delay(1000) // Wait for move to complete

        // Simulate tap: scale down and up
        fingerScale.animateTo(0.8f, animationSpec = tween(200))
        fingerScale.animateTo(1f, animationSpec = tween(200))

        // Trigger pop on the bubble
        coroutineScope.launch {
            firstBubble.isPopping = true
            firstBubble.scale.animateTo(0f, animationSpec = tween(300))
        }
        // Spawn pop particles
        repeat(Random.nextInt(8, 12)) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val particleSpeed = Random.nextFloat() * 10 + 5
            val vx = cos(angle) * particleSpeed
            val vy = sin(angle) * particleSpeed
            val particleRadius = Random.nextFloat() * 10 + 5
            particles.add(BubbleParticle(firstBubble.x, firstBubble.y, vx, vy, particleRadius))
        }
        // Play sound
        if (popSound.isPlaying) {
            popSound.pause()
            popSound.seekTo(0)
        }
        popSound.start()

        delay(500) // Short delay after pop

        // Animate finger away (e.g., fade or move off)
        fingerX.animateTo(fingerX.value + 300f, animationSpec = tween(500)) // Move right off-screen

        onComplete()
    }

    Image(
        imageVector = Icons.Default.TouchApp,
        contentDescription = "Tutorial Finger",
        modifier = Modifier
            .offset { Offset(fingerX.value, fingerY.value).toIntOffset() }
            .size(fingerSize)
            .scale(fingerScale.value),
        contentScale = ContentScale.Fit
    )
}

// Helper extension for Offset to IntOffset
private fun Offset.toIntOffset() = androidx.compose.ui.unit.IntOffset(x.toInt(), y.toInt())

data class Bubble(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speed: Float,
    val scale: Animatable<Float, AnimationVector1D> = Animatable(1f),
    var isPopping: Boolean = false
)

data class BubbleParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float = 1f
)

// Helper to check if a new bubble will collide with any existing
private fun willCollideWithAny(newBubble: Bubble, existingBubbles: List<Bubble>): Boolean {
    existingBubbles.forEach { existing ->
        val dx = abs(newBubble.x - existing.x)
        val deltaY0 = newBubble.y - existing.y // Positive, new is below
        val relVel = existing.speed - newBubble.speed // existing_speed - new_speed
        val rSum = newBubble.radius + existing.radius
        if (relVel < 0) { // New is faster, will catch up
            if (dx < rSum) return true
        } else { // Same speed or existing faster; check min dist at closest (initial)
            val minDist = sqrt(dx.pow(2) + deltaY0.pow(2))
            if (minDist < rSum) return true
        }
    }
    return false
}