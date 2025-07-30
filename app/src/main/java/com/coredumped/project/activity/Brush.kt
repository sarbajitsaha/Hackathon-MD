package com.coredumped.project.activity

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

// Logging tag
private const val TAG = "Brush"

// Particle represents a playful, glowing drop.
data class Particle(
    var pos: Offset,
    var vel: Offset,
    var alpha: Float,
    var size: Float,
    val color: Color
)

private const val MAX_PARTICLES = 1000 // Reduced from 7000 to improve performance

@Composable
fun FluidSimulationScreen(navController: NavController) {
    // Performance tracking variables
    var lastFrameTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var frameCount by remember { mutableStateOf(0) }
    var fps by remember { mutableStateOf(0) }
    var lastLogTime by remember { mutableStateOf(0L) }

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val particles = remember { mutableStateListOf<Particle>() }
    var showHint by remember { mutableStateOf(true) }
    var dragPosition by remember { mutableStateOf<Offset?>(null) }
    val density = LocalDensity.current

    // Animation for tutorial finger
    val fingerPosition = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var showTutorial by remember { mutableStateOf(true) }
    var hasInteracted by remember { mutableStateOf(false) }
    var tutorialCompleted by remember { mutableStateOf(false) }

    // Log initial setup
    LaunchedEffect(Unit) {
        Log.d(TAG, "FluidSimulation initialized")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Canvas with a dark blue gradient background.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D47A1), // dark blue
                            Color(0xFF1976D2)  // medium dark blue
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            Log.d(TAG, "Drag START detected")
                            showHint = false
                            showTutorial = false
                            hasInteracted = true
                            tutorialCompleted = true
                            dragPosition = offset

                            val burstTime = measureTimeMillis {
                                addBurst(offset, particles)
                            }
                            Log.d(TAG, "Added burst of particles in $burstTime ms, total particles: ${particles.size}")
                        },
                        onDrag = { change, _ ->
                            dragPosition = change.position
                            // Limit particle generation rate to reduce lag
                            if (Random.nextFloat() > 0.2f) { // Only add particles 80% of the time
                                val addTime = measureTimeMillis {
                                    addParticle(change.position, particles)
                                }

                                // Log occasionally during drag to avoid flooding logs
                                val now = System.currentTimeMillis()
                                if (now - lastLogTime > 500) { // Log every 500ms during drag
                                    Log.d(TAG, "Added particle in $addTime ms, total particles: ${particles.size}, FPS: $fps")
                                    lastLogTime = now
                                }
                            }
                        },
                        onDragEnd = {
                            Log.d(TAG, "Drag END detected. Particle count: ${particles.size}")
                            dragPosition = null
                        },
                        onDragCancel = {
                            Log.d(TAG, "Drag CANCELLED")
                            dragPosition = null
                        }
                    )
                }
        ) {
            canvasSize = size

            val startTime = System.currentTimeMillis()

            // More aggressive particle management to prevent lag
            if (particles.size > MAX_PARTICLES) {
                val removeCount = min(particles.size - MAX_PARTICLES, 50)
                val removeTime = measureTimeMillis {
                    repeat(removeCount) { particles.removeAt(0) }
                }
                Log.d(TAG, "Removed $removeCount excess particles in $removeTime ms")
            }

            // Draw each particle with an outer glow and inner core.
            val drawTime = measureTimeMillis {
                particles.forEach { particle ->
                    // Outer glow (drawn larger and with lower alpha)
                    drawCircle(
                        color = particle.color.copy(alpha = particle.alpha * 0.3f),
                        center = particle.pos,
                        radius = particle.size * 2f
                    )
                    // Inner core
                    drawCircle(
                        color = particle.color.copy(alpha = particle.alpha),
                        center = particle.pos,
                        radius = particle.size
                    )
                }
            }

            // Calculate frame time and FPS
            val currentTime = System.currentTimeMillis()
            val frameDuration = currentTime - lastFrameTime
            lastFrameTime = currentTime
            frameCount++

            // Update FPS every second
            if (currentTime - lastLogTime > 1000) {
                fps = frameCount
                frameCount = 0
                lastLogTime = currentTime

                // Log overall rendering stats
                Log.d(TAG, "Drawing: FPS=$fps, Particles=${particles.size}, Draw time=$drawTime ms")
            }

            // Log if drawing took too long (potential performance issue)
            if (drawTime > 16) { // 16ms = target for 60fps
                Log.w(TAG, "Drawing took $drawTime ms (>16ms), may cause lag. Particles: ${particles.size}")
            }
        }

        // Tutorial animation showing finger dragging - only shown during tutorial
        if (showTutorial && !tutorialCompleted && canvasSize != Size.Zero) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(fingerPosition.value.x.roundToInt() - 50, fingerPosition.value.y.roundToInt() - 50) }
                    .size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = "Drag to explore",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Tutorial animation effect
        LaunchedEffect(canvasSize, showTutorial) {
            if (canvasSize != Size.Zero && showTutorial && !tutorialCompleted) {
                Log.d(TAG, "Starting tutorial animation")

                // Initial position in center
                val startX = canvasSize.width * 0.3f
                val startY = canvasSize.height * 0.5f
                fingerPosition.snapTo(Offset(startX, startY))

                // Create a demo path
                delay(1000) // Wait a moment before starting the demo

                // Animate finger from left to right
                fingerPosition.animateTo(
                    targetValue = Offset(canvasSize.width * 0.7f, canvasSize.height * 0.5f),
                    animationSpec = tween(2000)
                ) {
                    // Add particles along the path - less frequently to prevent lag
                    if (!hasInteracted && Random.nextFloat() > 0.5f) {
                        addParticle(value, particles)
                    }
                }

                if (!hasInteracted) {
                    delay(500)
                    // Move to another position and create more particles
                    fingerPosition.animateTo(
                        targetValue = Offset(canvasSize.width * 0.5f, canvasSize.height * 0.3f),
                        animationSpec = tween(1500)
                    ) {
                        if (!hasInteracted && Random.nextFloat() > 0.5f) {
                            addParticle(value, particles)
                        }
                    }

                    // End the tutorial and show the hint
                    if (!hasInteracted) {
                        showTutorial = false
                        tutorialCompleted = true
                        showHint = true
                        Log.d(TAG, "Tutorial completed without interaction")
                    }
                }
            }
        }

        // Particle physics update loop with optimizations for finger lift case
        LaunchedEffect(Unit) {
            Log.d(TAG, "Starting physics update loop")
            val toRemove = ArrayList<Particle>()
            var updateCount = 0
            var lastUpdateLogTime = 0L

            while (isActive) {
                withFrameNanos { frameTimeNanos ->
                    val updateStartTime = System.currentTimeMillis()
                    toRemove.clear()
                    updateCount++

                    // Process all particles
                    val physicsTime = measureTimeMillis {
                        particles.forEach { p ->
                            // Update position based on velocity
                            p.pos += p.vel

                            // Apply gentle friction but keep particles moving
                            p.vel *= 0.995f

                            // Add continuous motion for better visual effect - reduced frequency
                            if (Random.nextFloat() > 0.7f) { // Only 30% of frames get random motion to reduce CPU load
                                p.vel += Offset(
                                    Random.nextFloat() * 0.15f - 0.075f,
                                    Random.nextFloat() * 0.15f - 0.12f  // Slightly biased upward
                                )
                            }

                            // Apply a slower fade
                            p.alpha *= 0.996f

                            // Collect particles to remove
                            if (p.alpha < 0.02f ||
                                p.pos.x < -50 || p.pos.x > canvasSize.width + 50 ||
                                p.pos.y < -50 || p.pos.y > canvasSize.height + 50) {
                                toRemove.add(p)
                            }
                        }
                    }

                    // Log particle removal metrics
                    val removalStartTime = System.currentTimeMillis()
                    var removalCount = toRemove.size

                    // Limit number of particles to remove per frame to avoid lag spikes
                    if (toRemove.size > 50) {
                        Log.w(TAG, "Large number of particles to remove: ${toRemove.size}, limiting to 50")
                        toRemove.shuffle() // Randomly select particles to remove
                        toRemove.subList(50, toRemove.size).clear() // Keep only 50 to remove
                        removalCount = 50
                    }

                    // Remove collected particles
                    val removalTime = measureTimeMillis {
                        particles.removeAll(toRemove)
                    }

                    // Calculate total update time
                    val totalUpdateTime = System.currentTimeMillis() - updateStartTime

                    // Log physics update metrics periodically (every 60 frames ~ 1 second)
                    val now = System.currentTimeMillis()
                    if (updateCount % 60 == 0 || removalCount > 10 || now - lastUpdateLogTime > 1000) {
                        Log.d(TAG, "Physics update: Particles=${particles.size}, " +
                                "Physics=${physicsTime}ms, Removals=$removalCount, " +
                                "Removal time=${removalTime}ms, Total=${totalUpdateTime}ms")
                        lastUpdateLogTime = now
                    }

                    // Log warning if update took too long
                    if (totalUpdateTime > 16) {
                        Log.w(TAG, "Physics update took ${totalUpdateTime}ms (>16ms), may cause lag. " +
                                "Particles=${particles.size}, Removals=$removalCount")
                    }

                    // Extra detailed logging during finger lift (detect by large removals after drag)
                    if (dragPosition == null && removalCount > 20) {
                        Log.d(TAG, "Finger lifted: Processing $removalCount removals in ${removalTime}ms, " +
                                "Remaining particles: ${particles.size}")
                    }
                }
            }
        }

        // Tutorial hint icon (only if user hasn't touched yet and tutorial is done)
        if (showHint && tutorialCompleted && !hasInteracted) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Touch and drag to create magic!",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
            )
        }

        // Colorful back button at the top-left.
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
                .clickable {
                    Log.d(TAG, "Back button clicked, exiting simulation")
                    navController.popBackStack()
                }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Adds a single playful particle at the given offset.
private fun addParticle(offset: Offset, particles: MutableList<Particle>) {
    val color = generatePlayfulColor()
    val angle = Random.nextDouble(0.0, 2 * PI)
    val speed = Random.nextFloat() * 2f + 1f
    val vel = Offset(
        (cos(angle) * speed).toFloat(),
        (sin(angle) * speed).toFloat()
    )
    particles.add(
        Particle(
            pos = offset,
            vel = vel,
            alpha = 1f,
            size = Random.nextFloat() * 10f + 20f,
            color = color
        )
    )
}

// Adds several particles for a burst effect.
private fun addBurst(offset: Offset, particles: MutableList<Particle>) {
    val burstCount = Random.nextInt(8, 15)
    Log.d(TAG, "Adding burst of $burstCount particles")
    repeat(burstCount) {
        addParticle(offset, particles)
    }
}

// Returns a random pastel color for a playful feel.
private fun generatePlayfulColor(): Color {
    val pastelColors = listOf(
        Color(0xFFFFCDD2),
        Color(0xFFF8BBD0),
        Color(0xFFE1BEE7),
        Color(0xFFD1C4E9),
        Color(0xFFC5CAE9),
        Color(0xFFBBDEFB),
        Color(0xFFB3E5FC),
        Color(0xFFB2EBF2),
        Color(0xFFB2DFDB),
        Color(0xFFC8E6C9),
        Color(0xFFDCEDC8),
        Color(0xFFF0F4C3),
        Color(0xFFFFF9C4),
        Color(0xFFFFECB3)
    )
    return pastelColors.random()
}

// Helper to obtain the Activity from a Context.
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}