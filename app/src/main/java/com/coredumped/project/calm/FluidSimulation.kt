package com.coredumped.project.calm

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.math.*
import kotlin.random.Random
import kotlinx.coroutines.isActive

data class Particle(
    var pos: Offset,
    var vel: Offset,
    var alpha: Float,
    var size: Float,
    val color: Color,
    val glowColor: Color,
    var trail: List<Offset> = listOf()
)

data class Line(
    val points: MutableList<Offset>,
    val color: Color,
    val glowColor: Color,
    val thickness: Float = Random.nextFloat() * 8f + 8f,
    var disintegrating: Boolean = true
)

@Composable
fun FluidSimulationScreen(navController: NavController) {
    // Force landscape mode
    val context = LocalContext.current
    SideEffect {
        context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    var currentColor by remember { mutableStateOf(Color.White) }
    var currentGlowColor by remember { mutableStateOf(Color.White) }
    val lines = remember { mutableStateListOf<Line>() }
    val particles = remember { mutableStateListOf<Particle>() }
    var showHint by remember { mutableStateOf(true) }
    var frameCount by remember { mutableStateOf(0) }

    // Performance monitoring (optional)
    var lastFrameTime by remember { mutableStateOf(0L) }
    var frameTimeAvg by remember { mutableStateOf(0f) }

    // Animation for glowing back button
    val infiniteTransition = rememberInfiniteTransition(label = "backButtonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )

    // Animation for hint icon
    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlphaAnimation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            showHint = false
                            currentPoints.clear()
                            currentPoints.add(offset)
                            // Generate vibrant color with matching glow
                            val colorPair = generateVibrantColorWithGlow()
                            currentColor = colorPair.first
                            currentGlowColor = colorPair.second
                        },
                        onDrag = { change, _ ->
                            val lastPoint = currentPoints.lastOrNull()
                            if (lastPoint == null || (change.position - lastPoint).getDistance() > 2f) {
                                currentPoints.add(change.position)

                                // Create particles while drawing for immediate feedback
                                if (Random.nextFloat() < 0.3f) { // Increased particle creation rate
                                    val pos = change.position
                                    repeat(Random.nextInt(1, 3)) { // Fewer particles while drawing for performance
                                        val angle = Random.nextDouble() * 2 * Math.PI
                                        val speed = Random.nextFloat() * 1.5f + 0.5f
                                        val vel = Offset(
                                            (cos(angle) * speed).toFloat(),
                                            (sin(angle) * speed).toFloat()
                                        )

                                        particles.add(
                                            Particle(
                                                pos = pos + Offset(Random.nextFloat() * 4 - 2, Random.nextFloat() * 4 - 2),
                                                vel = vel,
                                                alpha = 0.8f + Random.nextFloat() * 0.2f,
                                                size = Random.nextFloat() * 3f + 3f,
                                                color = currentColor,
                                                glowColor = currentGlowColor,
                                                trail = listOf()
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            if (currentPoints.size > 1) {
                                lines.add(
                                    Line(
                                        currentPoints.toMutableList(),
                                        currentColor,
                                        currentGlowColor,
                                        thickness = Random.nextFloat() * 8f + 8f,
                                        disintegrating = true
                                    )
                                )

                                // Create initial burst of particles
                                createGlowingParticleBurst(currentPoints.last(), currentColor, currentGlowColor, particles)
                            }
                            currentPoints.clear()
                        },
                        onDragCancel = {
                            currentPoints.clear()
                        }
                    )
                }
        ) {
            canvasSize = size

            // Draw existing lines with enhanced glow effect
            for (line in lines) {
                if (line.points.isNotEmpty()) {
                    val path = createSmoothPath(line.points)

                    // Enhanced multi-layered glow effect
                    // Layer 1 - Outer glow (large, subtle)
                    drawPath(
                        path = path,
                        color = line.glowColor.copy(alpha = 0.2f),
                        style = Stroke(
                            width = line.thickness + 18f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Layer 2 - Mid glow (medium, more visible)
                    drawPath(
                        path = path,
                        color = line.glowColor.copy(alpha = 0.4f),
                        style = Stroke(
                            width = line.thickness + 12f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Layer 3 - Inner glow (small, intense)
                    drawPath(
                        path = path,
                        color = line.glowColor.copy(alpha = 0.6f),
                        style = Stroke(
                            width = line.thickness + 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Main line (solid)
                    drawPath(
                        path = path,
                        color = line.color,
                        style = Stroke(
                            width = line.thickness,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Draw current line being drawn with enhanced glow effect
            if (currentPoints.isNotEmpty()) {
                val path = createSmoothPath(currentPoints)
                val thickness = Random.nextFloat() * 8f + 8f

                // Enhanced multi-layered glow effect (same as for completed lines)
                // Layer 1 - Outer glow
                drawPath(
                    path = path,
                    color = currentGlowColor.copy(alpha = 0.2f),
                    style = Stroke(
                        width = thickness + 18f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Layer 2 - Mid glow
                drawPath(
                    path = path,
                    color = currentGlowColor.copy(alpha = 0.4f),
                    style = Stroke(
                        width = thickness + 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Layer 3 - Inner glow
                drawPath(
                    path = path,
                    color = currentGlowColor.copy(alpha = 0.6f),
                    style = Stroke(
                        width = thickness + 6f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Main line
                drawPath(
                    path = path,
                    color = currentColor,
                    style = Stroke(
                        width = thickness,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Optimized particle rendering - adapt detail level based on particle count
            val particleDrawingMode = when {
                particles.size > 5000 -> 2 // Ultra simplified
                particles.size > 2000 -> 1 // Simplified
                else -> 0 // Full detail
            }

            // Draw particles with optimized glow effects
            for (particle in particles) {
                when (particleDrawingMode) {
                    0 -> {
                        // Full detail mode
                        // Draw trails with glow
                        val trailToDraw = particle.trail.takeLast(min(4, particle.trail.size))
                        trailToDraw.forEachIndexed { index, trailPos ->
                            val trailProgress = index.toFloat() / max(1, trailToDraw.size - 1)
                            val trailAlpha = particle.alpha * 0.6f * (1 - trailProgress)
                            val trailSize = particle.size * 0.7f * (1 - trailProgress)

                            // Draw simplified trail glow
                            drawCircle(
                                color = particle.glowColor.copy(alpha = trailAlpha * 0.4f),
                                center = trailPos,
                                radius = trailSize * 1.8f
                            )

                            // Main trail point
                            drawCircle(
                                color = particle.color.copy(alpha = trailAlpha),
                                center = trailPos,
                                radius = trailSize
                            )
                        }

                        // Enhanced particle glow - multiple layers for bloom effect
                        // Outer glow layer (large, subtle)
                        drawCircle(
                            color = particle.glowColor.copy(alpha = particle.alpha * 0.2f),
                            center = particle.pos,
                            radius = particle.size * 2.5f
                        )

                        // Middle glow layer (medium, stronger)
                        drawCircle(
                            color = particle.glowColor.copy(alpha = particle.alpha * 0.4f),
                            center = particle.pos,
                            radius = particle.size * 1.7f
                        )

                        // Inner glow layer (small, intense)
                        drawCircle(
                            color = particle.glowColor.copy(alpha = particle.alpha * 0.7f),
                            center = particle.pos,
                            radius = particle.size * 1.3f
                        )

                        // Core particle (solid)
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha),
                            center = particle.pos,
                            radius = particle.size
                        )
                    }
                    1 -> {
                        // Simplified mode - fewer glow layers
                        // Draw only last trail point
                        if (particle.trail.isNotEmpty()) {
                            val lastTrail = particle.trail.last()
                            drawCircle(
                                color = particle.color.copy(alpha = particle.alpha * 0.4f),
                                center = lastTrail,
                                radius = particle.size * 0.8f
                            )
                        }

                        // Simplified glow (just two layers)
                        drawCircle(
                            color = particle.glowColor.copy(alpha = particle.alpha * 0.3f),
                            center = particle.pos,
                            radius = particle.size * 2.0f
                        )

                        // Inner glow + core in one
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha),
                            center = particle.pos,
                            radius = particle.size * 1.1f
                        )
                    }
                    else -> {
                        // Ultra simplified - just core particle with minimal glow
                        drawCircle(
                            color = particle.glowColor.copy(alpha = particle.alpha * 0.3f),
                            center = particle.pos,
                            radius = particle.size * 1.5f
                        )

                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha),
                            center = particle.pos,
                            radius = particle.size
                        )
                    }
                }
            }
        }

        // Optimized animation loop for updates
        LaunchedEffect(Unit) {
            // Preallocate these collections
            val linesToRemove = ArrayList<Line>()
            val updatedParticles = ArrayList<Particle>(2000)
            val pointsToRemove = ArrayList<Offset>(10)

            while (isActive) {
                withFrameNanos { frameTime ->
                    // Track frame time for performance monitoring
                    if (lastFrameTime != 0L) {
                        val deltaMs = (frameTime - lastFrameTime) / 1_000_000
                        frameTimeAvg = frameTimeAvg * 0.9f + deltaMs * 0.1f
                    }
                    lastFrameTime = frameTime

                    // Clear collections for reuse
                    linesToRemove.clear()
                    updatedParticles.clear()

                    val bounds = Rect(-100f, -100f, canvasSize.width + 100f, canvasSize.height + 100f)

                    // Process lines in batches (max 10 lines per frame)
                    var processedLines = 0
                    val maxLinesToProcess = min(10, lines.size)

                    // Update lines and disintegrate
                    for (i in 0 until lines.size) {
                        val line = lines[i]
                        if (line.disintegrating && line.points.isNotEmpty()) {
                            // Calculate disintegration rate - more points for longer lines
                            val disintegrationRate = max(2, min(8, line.points.size / 10))

                            // Clear the points collection for reuse
                            pointsToRemove.clear()

                            if (line.points.size <= disintegrationRate) {
                                // Remove all remaining points at once
                                pointsToRemove.addAll(line.points)
                                line.points.clear()
                            } else {
                                // Remove only some points
                                repeat(disintegrationRate) {
                                    if (line.points.isEmpty()) return@repeat

                                    // Select points in sequence for smoother disintegration
                                    val pointIndex = when (it % 3) {
                                        0 -> 0 // Front of line
                                        1 -> line.points.size - 1 // End of line
                                        else -> line.points.size / 2 // Middle of line
                                    }

                                    if (pointIndex >= line.points.size) return@repeat
                                    pointsToRemove.add(line.points.removeAt(pointIndex))
                                }
                            }

                            // Create particles from removed points
                            createParticlesFromPoints(pointsToRemove, line.color, line.glowColor,
                                line.thickness, particles)
                        }

                        if (line.points.isEmpty()) {
                            linesToRemove.add(line)
                        }

                        // Count processed lines
                        processedLines++
                        if (processedLines >= maxLinesToProcess) break
                    }

                    lines.removeAll(linesToRemove)

                    // Pre-compute these constants once outside the loop
                    val gravity = Offset(0.02f, 0.08f)
                    val wind = Offset(0.03f, -0.02f)
                    val netForce = gravity + wind
                    val alphaDecayFactor = 0.99f  // Slower decay for smoother transitions
                    val sizeDecay = 0.995f
                    val drag = 0.98f

                    // Limit particles if there are too many
                    val maxParticles = 10000
                    if (particles.size > maxParticles) {
                        // Remove oldest particles to stay under the limit
                        val toRemove = particles.size - maxParticles
                        repeat(toRemove) {
                            if (particles.isNotEmpty()) {
                                particles.removeAt(0)
                            }
                        }
                    }

                    // Process particles in batches - all particles but limit updates per frame
                    val maxParticleUpdates = min(particles.size, 2000).coerceAtLeast(1) // Ensure at least 1, never 0
                    val particleStep = if (particles.isEmpty()) 1 else max(1, particles.size / maxParticleUpdates)

                    // Add randomness offset based on frame to avoid synchronized updates
                    val randomOffset = if (particles.isEmpty()) 0 else frameCount % particleStep

                    // Update particles efficiently
                    for (i in randomOffset until particles.size step particleStep) {
                        val particle = particles[i]

                        // Update velocity with forces
                        particle.vel = (particle.vel + netForce) * drag

                        // Add small random movement occasionally
                        if (frameCount % 3 == 0 && Random.nextFloat() < 0.3f) {
                            particle.vel += Offset(
                                Random.nextFloat() * 0.06f - 0.03f,
                                Random.nextFloat() * 0.06f - 0.03f
                            )
                        }

                        // Update trail more efficiently
                        val newTrail = if (particle.trail.size >= 6) {
                            val newList = ArrayList<Offset>(6)
                            // Skip the oldest position and add all others
                            for (j in 1 until particle.trail.size) {
                                newList.add(particle.trail[j])
                            }
                            // Add current position
                            newList.add(particle.pos)
                            newList
                        } else {
                            // Just add current position to trail
                            particle.trail + particle.pos
                        }

                        // Update position
                        val newPos = particle.pos + particle.vel

                        // Update properties with faster calculations
                        val newAlpha = particle.alpha * alphaDecayFactor
                        val newSize = particle.size * sizeDecay

                        // Only keep if still visible and in bounds
                        if (newAlpha > 0.01f && bounds.contains(newPos)) {
                            updatedParticles.add(
                                Particle(
                                    pos = newPos,
                                    vel = particle.vel,
                                    alpha = newAlpha,
                                    size = newSize,
                                    color = particle.color,
                                    glowColor = particle.glowColor,
                                    trail = newTrail
                                )
                            )
                        }
                    }

                    // Occasionally add ambient particles for more richness
                    if (frameCount % 20 == 0 && particles.size < maxParticles - 100 && Random.nextFloat() < 0.1f) {
                        addAmbientParticles(canvasSize, particles)
                    }

                    // Update non-processed particles - they continue with their current velocity
                    for (i in 0 until particles.size step particleStep) {
                        if (i == randomOffset) continue // Skip the ones we've already processed

                        val particle = particles[i]
                        val newPos = particle.pos + particle.vel

                        // Only keep if in bounds
                        if (bounds.contains(newPos)) {
                            updatedParticles.add(particle.copy(pos = newPos))
                        }
                    }

                    // Replace particles with updated ones
                    particles.clear()
                    particles.addAll(updatedParticles)

                    // Increment frame counter
                    frameCount++
                }
            }
        }

        // Touch hint with animation
        if (showHint) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Touch to start",
                tint = Color.White.copy(alpha = hintAlpha),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
                    .scale(1f + (hintAlpha - 0.4f) * 0.1f)
            )
        }

        // Enhanced glowing back button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            // Extra outer glow
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .scale(pulseScale * 1.4f)
                    .alpha(0.2f)
                    .blur(12.dp),
                containerColor = Color(0xFF5D9FD6),
                contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }

            // Outer glow
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .scale(pulseScale * 1.2f)
                    .alpha(0.4f)
                    .blur(8.dp),
                containerColor = Color(0xFF3D85C6),
                contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }

            // Main button
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.scale(pulseScale),
                containerColor = Color(0xFF2B5F8E),
                contentColor = Color.White
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

// Create particles from a list of points more efficiently
private fun createParticlesFromPoints(
    points: List<Offset>,
    baseColor: Color,
    glowColor: Color,
    thickness: Float,
    particles: MutableList<Particle>
) {
    // Process points in batches to avoid creating too many particles at once
    val maxParticlesToCreate = 150
    if (particles.size > 3000) return // Skip if already too many particles

    val pointsToDraw = min(points.size, 20)
    val step = max(1, points.size / pointsToDraw)
    var particlesCreated = 0

    for (i in 0 until points.size step step) {
        if (particlesCreated >= maxParticlesToCreate) break

        val point = points[i % points.size]
        val particleCount = Random.nextInt(2, 5)

        repeat(particleCount) {
            if (particlesCreated >= maxParticlesToCreate) return@repeat

            val angle = Random.nextDouble() * 2 * Math.PI
            val speed = Random.nextFloat() * 2.5f + 1.5f

            val initialVel = Offset(
                (cos(angle) * speed).toFloat(),
                (sin(angle) * speed).toFloat()
            )

            // Create varied glowing colors
            val colorVariation = 0.12f

            // Varied main color
            val variedColor = Color(
                red = (baseColor.red + Random.nextFloat() * colorVariation * 2 - colorVariation).coerceIn(0f, 1f),
                green = (baseColor.green + Random.nextFloat() * colorVariation * 2 - colorVariation).coerceIn(0f, 1f),
                blue = (baseColor.blue + Random.nextFloat() * colorVariation * 2 - colorVariation).coerceIn(0f, 1f)
            )

            // Varied glow color - make it slightly brighter
            val variedGlowColor = Color(
                red = (glowColor.red + Random.nextFloat() * colorVariation).coerceIn(0f, 1f),
                green = (glowColor.green + Random.nextFloat() * colorVariation).coerceIn(0f, 1f),
                blue = (glowColor.blue + Random.nextFloat() * colorVariation).coerceIn(0f, 1f)
            )

            particles.add(
                Particle(
                    pos = point + Offset(
                        Random.nextFloat() * 4 - 2,
                        Random.nextFloat() * 4 - 2
                    ),
                    vel = initialVel,
                    alpha = 0.9f + Random.nextFloat() * 0.1f,
                    size = thickness * 0.3f + Random.nextFloat() * 2.5f,
                    color = variedColor,
                    glowColor = variedGlowColor,
                    trail = listOf(point)
                )
            )

            particlesCreated++
        }
    }
}

// Create a burst of glowing particles
private fun createGlowingParticleBurst(position: Offset, baseColor: Color, glowColor: Color, particles: MutableList<Particle>) {
    val burstSize = if (particles.size < 3000) 30 else 15 // Reduced count if already many particles

    repeat(burstSize) {
        val angle = Random.nextDouble() * 2 * Math.PI
        val speed = Random.nextFloat() * 4f + 3f

        val velocity = Offset(
            (cos(angle) * speed).toFloat(),
            (sin(angle) * speed).toFloat()
        )

        // Add color variation
        val colorVariation = 0.12f
        val variedColor = Color(
            red = (baseColor.red + Random.nextFloat() * colorVariation * 2 - colorVariation).coerceIn(0f, 1f),
            green = (baseColor.green + Random.nextFloat() * colorVariation * 2 - colorVariation).coerceIn(0f, 1f),
            blue = (baseColor.blue + Random.nextFloat() * colorVariation * 2 - colorVariation).coerceIn(0f, 1f)
        )

        // Varied glow color - make it slightly brighter
        val variedGlowColor = Color(
            red = (glowColor.red + Random.nextFloat() * colorVariation).coerceIn(0f, 1f),
            green = (glowColor.green + Random.nextFloat() * colorVariation).coerceIn(0f, 1f),
            blue = (glowColor.blue + Random.nextFloat() * colorVariation).coerceIn(0f, 1f)
        )

        particles.add(
            Particle(
                pos = position + Offset(Random.nextFloat() * 6 - 3, Random.nextFloat() * 6 - 3),
                vel = velocity,
                alpha = 1f,
                size = Random.nextFloat() * 5f + 5f,
                color = variedColor,
                glowColor = variedGlowColor,
                trail = listOf(position)
            )
        )
    }
}

// Add ambient particles occasionally for more visual richness
private fun addAmbientParticles(canvasSize: Size, particles: MutableList<Particle>) {
    val colorPair = generateVibrantColorWithGlow()
    repeat(Random.nextInt(10, 30)) {
        val x = Random.nextFloat() * canvasSize.width
        val y = Random.nextFloat() * canvasSize.height

        val angle = Random.nextDouble() * 2 * Math.PI
        val speed = Random.nextFloat() * 1.0f + 0.5f

        particles.add(
            Particle(
                pos = Offset(x, y),
                vel = Offset(
                    (cos(angle) * speed).toFloat(),
                    (sin(angle) * speed).toFloat()
                ),
                alpha = 0.7f + Random.nextFloat() * 0.3f,
                size = Random.nextFloat() * 3f + 2f,
                color = colorPair.first,
                glowColor = colorPair.second,
                trail = listOf()
            )
        )
    }
}

// Generate vibrant color with matching glow color
private fun generateVibrantColorWithGlow(): Pair<Color, Color> {
    // Create a neon-like color palette
    val colorOptions = listOf(
        // Color, Glow
        Color(0xFFFF1177) to Color(0xFFFF71B7), // Pink
        Color(0xFF33CCFF) to Color(0xFF99EEFF), // Cyan
        Color(0xFFFFCC00) to Color(0xFFFFEE99), // Yellow
        Color(0xFF33FF33) to Color(0xFF99FF99), // Green
        Color(0xFFFF3333) to Color(0xFFFF9999), // Red
        Color(0xFF3333FF) to Color(0xFF9999FF), // Blue
        Color(0xFFFF33FF) to Color(0xFFFF99FF), // Magenta
        Color(0xFF33FFFF) to Color(0xFF99FFFF), // Aqua
        Color(0xFFFF66CC) to Color(0xFFFFB8E1)  // Light Pink
    )

    // Randomly select a color from our vibrant options
    return if (Random.nextFloat() < 0.7f) {
        // 70% chance to use predefined vibrant colors
        colorOptions.random()
    } else {
        // 30% chance to generate random vibrant color
        val primaryChannel = Random.nextInt(3) // 0=red, 1=green, 2=blue

        val red = if (primaryChannel == 0)
            Random.nextFloat() * 0.3f + 0.7f // 0.7-1.0 (brighter)
        else
            Random.nextFloat() * 0.4f // 0.0-0.4 (darker)

        val green = if (primaryChannel == 1)
            Random.nextFloat() * 0.3f + 0.7f // 0.7-1.0 (brighter)
        else
            Random.nextFloat() * 0.4f // 0.0-0.4 (darker)

        val blue = if (primaryChannel == 2)
            Random.nextFloat() * 0.3f + 0.7f // 0.7-1.0 (brighter)
        else
            Random.nextFloat() * 0.4f // 0.0-0.4 (darker)

        val mainColor = Color(red, green, blue)

        // Create a brighter glow color based on the main color
        val glowColor = Color(
            red = (red + 0.2f).coerceAtMost(1f),
            green = (green + 0.2f).coerceAtMost(1f),
            blue = (blue + 0.2f).coerceAtMost(1f)
        )

        mainColor to glowColor
    }
}

// Helper function to create a smoother path from points
private fun createSmoothPath(points: List<Offset>): Path {
    if (points.size < 2) return Path().apply { moveTo(points.firstOrNull()?.x ?: 0f, points.firstOrNull()?.y ?: 0f) }

    return Path().apply {
        moveTo(points[0].x, points[0].y)

        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
}

// Extension function to get distance between two offsets
private fun Offset.getDistance(): Float {
    return sqrt(x * x + y * y)
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}