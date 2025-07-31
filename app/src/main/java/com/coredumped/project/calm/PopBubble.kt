package com.coredumped.project.calm

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

val bubbleColors = listOf(
    Color(0xFF1C4359), // Darker Blue
    Color(0xFF6B1123), // Darker Red
    Color(0xFF202050), // Darker Purple
    Color(0xFF6B3E00), // Darker Orange
    Color(0xFF124A20), // Darker Green
    Color(0xFF6B1714), // Darker Red-Orange
    Color(0xFF6B5500), // Darker Yellow
    Color(0xFF004A47), // Darker Teal
    Color(0xFF421E53), // Darker Lavender
    Color(0xFF6B303A)  // Darker Coral
)

@Composable
fun PopBubbleScreen(navController: NavController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Calculate screen dimensions in pixels
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Calculate reference dimension for scaling (smaller of width/height)
    val minDimension = min(screenWidth, screenHeight)

    // Define bubble size range as percentage of screen size
    // Using 8-15% of min dimension for bubble radius
    val minBubbleRadius = minDimension * 0.06f
    val maxBubbleRadius = minDimension * 0.1f

    val popSound = remember { MediaPlayer.create(context, R.raw.bubble_pop) }
    val bubbles = remember { mutableStateListOf<Bubble>() }
    val particles = remember { mutableStateListOf<BubbleParticle>() }
    val coroutineScope = rememberCoroutineScope()
    val spawnDelay = remember { mutableStateOf(1000L) }
    val lastPopTime = remember { mutableStateOf(0L) }
    val popRate = remember { mutableStateOf(0f) }
    val updateTrigger = remember { mutableStateOf(0) }
    val showTutorial = remember { mutableStateOf(true) }
    val tutorialShown = remember { mutableStateOf(false) }

    // Remember screen dimensions for bubble spawning
    val screenDimensions = remember {
        ScreenDimensions(
            width = screenWidth,
            height = screenHeight,
            minRadius = minBubbleRadius,
            maxRadius = maxBubbleRadius
        )
    }

    // Game loop for updating bubbles and particles
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60 FPS
            val currentTime = System.currentTimeMillis()
            bubbles.forEach { bubble ->
                if (bubble.isTutorial.not() && !bubble.isPopping) {
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
            val y = screenDimensions.height + 50f // Start off bottom
            var attempts = 0
            var spawned = false
            while (attempts < 10 && !spawned) {
                attempts++
                // Dynamic bubble size based on screen dimensions
                val radius = Random.nextFloat() * (screenDimensions.maxRadius - screenDimensions.minRadius) + screenDimensions.minRadius
                val x = Random.nextFloat() * (screenDimensions.width - 2 * radius) + radius

                // Scale speed based on screen height (0.4-1.0% of screen height per frame)
                val minSpeed = screenDimensions.height * 0.004f
                val maxSpeed = screenDimensions.height * 0.01f
                val speed = Random.nextFloat() * (maxSpeed - minSpeed) + minSpeed

                // Select a random color from our darker colors
                val color = bubbleColors.random()
                val candidate = Bubble(x, y, radius, speed, color = color)
                if (!willCollideWithAny(candidate, bubbles)) {
                    bubbles.add(candidate)
                    spawned = true

                    // Start the tutorial after the first bubble is spawned
                    if (!tutorialShown.value && bubbles.size == 1) {
                        delay(500) // Give a moment for the bubble to rise a bit
                        tutorialShown.value = true
                    }
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

                                    // Dynamic particle size based on bubble size
                                    val minParticleRadius = bubble.radius * 0.05f
                                    val maxParticleRadius = bubble.radius * 0.1f

                                    // Spawn pop particles with the same color as the bubble
                                    repeat(Random.nextInt(8, 12)) {
                                        val angle = Random.nextFloat() * 2 * PI.toFloat()
                                        val particleSpeed = Random.nextFloat() * 10 + 5
                                        val vx = cos(angle) * particleSpeed
                                        val vy = sin(angle) * particleSpeed
                                        val particleRadius = Random.nextFloat() * (maxParticleRadius - minParticleRadius) + minParticleRadius
                                        particles.add(BubbleParticle(bubble.x, bubble.y, vx, vy, particleRadius, color = bubble.color))
                                    }
                                    // Restart sound if already playing
                                    if (popSound.isPlaying) {
                                        popSound.pause()
                                        popSound.seekTo(0)
                                    }
                                    popSound.start()
                                    lastPopTime.value = System.currentTimeMillis()

                                    // Hide tutorial after user pops a bubble
                                    if (showTutorial.value) {
                                        showTutorial.value = false
                                    }

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
                // Base semi-transparent bubble with the bubble's color
                drawCircle(
                    color = bubble.color.copy(alpha = alpha * 0.3f),
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
                // Draw particles with the particle's color
                drawCircle(
                    color = particle.color.copy(alpha = particle.alpha * 0.3f),
                    radius = particle.radius,
                    center = Offset(particle.x, particle.y)
                )
            }
        }

        // Colorful back button in the top left corner
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9500),  // Orange
                            Color(0xFFFF2D55),  // Pink
                            Color(0xFF5856D6)   // Purple
                        )
                    )
                )
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        // Show tutorial finger when tutorial is active and we have at least one bubble
        if (showTutorial.value && bubbles.isNotEmpty() && tutorialShown.value) {
            TutorialFinger(
                bubbles = bubbles,
                onComplete = { showTutorial.value = false },
                coroutineScope = coroutineScope,
                popSound = popSound,
                particles = particles,
                screenDimensions = screenDimensions
            )
        }
    }
}

// Helper class to hold screen dimensions and bubble size information
data class ScreenDimensions(
    val width: Float,
    val height: Float,
    val minRadius: Float,
    val maxRadius: Float
)

@Composable
fun TutorialFinger(
    bubbles: List<Bubble>,
    onComplete: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    popSound: MediaPlayer,
    particles: MutableList<BubbleParticle>,
    screenDimensions: ScreenDimensions
) {
    if (bubbles.isEmpty()) return

    val tutorialBubble = remember { bubbles.first() }

    val targetX = tutorialBubble.x
    val targetY = tutorialBubble.y
    tutorialBubble.isTutorial = true

    val density = LocalDensity.current
    // Scale finger size based on bubble size
    val fingerSize = (tutorialBubble.radius * 0.8f).dp
    val fingerSizePx = with(density) { fingerSize.toPx() }

    // Start finger off-screen to the right
    val fingerX = remember { Animatable(bubbles.first().x + 200f) }
    val fingerY = remember { Animatable(bubbles.first().y) }
    val fingerScale = remember { Animatable(1f) }

    // Add a pulsing effect to draw attention
    val infiniteTransition = rememberInfiniteTransition(label = "fingerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fingerPulse"
    )

    LaunchedEffect(Unit) {
        // Delay to allow bubble to become visible
        delay(300)

        launch {
            fingerX.animateTo(targetX, animationSpec = tween(1000))
        }
        launch {
            fingerY.animateTo(targetY, animationSpec = tween(1000))
        }

        delay(1000) // Wait for move to complete

        // Simulate tap: scale down and up more dramatically
        fingerScale.animateTo(0.7f, animationSpec = tween(200))
        fingerScale.animateTo(1f, animationSpec = tween(200))

        // Trigger pop on the bubble
        coroutineScope.launch {
            tutorialBubble.isPopping = true
            tutorialBubble.scale.animateTo(0f, animationSpec = tween(300))
        }

        // Dynamic particle size based on bubble size
        val minParticleRadius = tutorialBubble.radius * 0.05f
        val maxParticleRadius = tutorialBubble.radius * 0.1f

        // Spawn pop particles with the same color as the bubble
        repeat(Random.nextInt(8, 12)) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val particleSpeed = Random.nextFloat() * 10 + 5
            val vx = cos(angle) * particleSpeed
            val vy = sin(angle) * particleSpeed
            val particleRadius = Random.nextFloat() * (maxParticleRadius - minParticleRadius) + minParticleRadius
            particles.add(BubbleParticle(tutorialBubble.x, tutorialBubble.y, vx, vy, particleRadius, color = tutorialBubble.color))
        }

        // Play sound
        if (popSound.isPlaying) {
            popSound.pause()
            popSound.seekTo(0)
        }
        popSound.start()

        delay(800) // Longer delay after pop to make sure user sees what happened

        // Animate finger away
        fingerX.animateTo(fingerX.value + 300f, animationSpec = tween(500)) // Move right off-screen

        delay(200) // Short delay before completing tutorial
        onComplete()
    }

    // Enhanced finger with shadow and more visible styling
    Box(
        modifier = Modifier
            .offset { Offset(fingerX.value - fingerSizePx/2, fingerY.value - fingerSizePx/2).toIntOffset() }
            .size(fingerSize)
            .graphicsLayer {
                scaleX = fingerScale.value * pulseScale
                scaleY = fingerScale.value * pulseScale
            }
    ) {
        // Add a glow/shadow behind the finger
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier
                .size(fingerSize)
                .offset(2.dp, 2.dp)
        )

        // Main finger icon
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = "Tap to pop bubble",
            tint = Color.White,
            modifier = Modifier.size(fingerSize)
        )
    }
}

// Helper extension for Offset to IntOffset
private fun Offset.toIntOffset() = androidx.compose.ui.unit.IntOffset(x.toInt(), y.toInt())

data class Bubble(
    var x: Float,
    var y: Float,
    var radius: Float,
    var speed: Float,
    val color: Color = bubbleColors.random(), // Default to a random color
    val scale: Animatable<Float, AnimationVector1D> = Animatable(1f),
    var isPopping: Boolean = false,
    var isTutorial: Boolean = false
)

data class BubbleParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Color = bubbleColors.random(), // Default to a random color
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