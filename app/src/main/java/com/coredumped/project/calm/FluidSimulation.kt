package com.coredumped.project.calm

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

// Fluid simulation constants
private const val ITERATIONS = 12 // Solver iterations
private const val DIFFUSION = 0.0001f // Diffusion rate of the fluid
private const val VISCOSITY = 0.0000001f // Viscosity of the fluid
private const val DT = 0.1f // Time step
private const val FORCE_MULTIPLIER = 500f // Touch force multiplier
private const val DENSITY_MULTIPLIER = 50f // Touch density multiplier
private const val DAMPING = 0.99f // Velocity damping factor (higher = less damping)
private const val DENSITY_DECAY = 0.995f // Density decay factor
private const val AMBIENT_FLOW = 1.0f // Ambient flow strength to keep fluid moving
private const val AUTO_ADD_INTERVAL = 60 // Frames between auto-adding density

// Available color palettes
enum class ColorPalette(val colors: Array<Color>) {
    OCEAN(arrayOf(
        Color(0xFF072448), // Deep blue
        Color(0xFF054a91), // Medium blue
        Color(0xFF3e7cb1), // Light blue
        Color(0xFF81a4cd), // Pale blue
        Color(0xFF54d2d2), // Teal
        Color(0xFFffcb00), // Yellow
        Color(0xFFf8aa4b), // Orange
        Color(0xFFff6150)  // Red
    )),

    RAINBOW(arrayOf(
        Color(0xFF9400D3), // Violet
        Color(0xFF4B0082), // Indigo
        Color(0xFF0000FF), // Blue
        Color(0xFF00FF00), // Green
        Color(0xFFFFFF00), // Yellow
        Color(0xFFFF7F00), // Orange
        Color(0xFFFF0000)  // Red
    )),

    FIRE(arrayOf(
        Color(0xFF240002), // Dark red
        Color(0xFF4d0005), // Deep red
        Color(0xFF930007), // Medium red
        Color(0xFFe3000b), // Bright red
        Color(0xFFff4c0e), // Orange
        Color(0xFFff9426), // Light orange
        Color(0xFFffca18), // Yellow
        Color(0xFFfffa6c)  // Light yellow
    )),

    COOL(arrayOf(
        Color(0xFF0d0b33), // Deep purple
        Color(0xFF22186e), // Purple
        Color(0xFF364bcc), // Blue
        Color(0xFF0597d1), // Light blue
        Color(0xFF28e7eb), // Cyan
        Color(0xFF8ef1f2), // Light cyan
        Color(0xFFd3f9fa)  // White-cyan
    )),

    MONO(arrayOf(
        Color(0xFF000000), // Black
        Color(0xFF222222),
        Color(0xFF444444),
        Color(0xFF666666),
        Color(0xFF888888),
        Color(0xFFaaaaaa),
        Color(0xFFcccccc),
        Color(0xFFffffff)  // White
    ))
}

@Composable
fun FluidSimulationScreen(navController: NavController) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var previousTouchPosition by remember { mutableStateOf<Offset?>(null) }
    var touchVelocity by remember { mutableStateOf(Offset.Zero) }
    var frameCount by remember { mutableStateOf(0) }

    // Color palette selection
    var currentPalette by remember { mutableStateOf(ColorPalette.OCEAN) }

    // Grid size - higher for smoother appearance (but watch performance on older devices)
    val gridSize = remember { 80 }

    // Create the fluid simulator
    val fluidSim = remember { FluidSimulator(gridSize) }

    // Update palette when it changes
    LaunchedEffect(currentPalette) {
        fluidSim.setPalette(currentPalette.colors)
    }

    // Reset simulator when canvas size changes
    LaunchedEffect(canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            fluidSim.reset()

            // Pre-warm the simulation with smoother initial state

            // Add some gentle ambient flow first
            for (i in 0 until 20) {
                val x = Random.nextInt(gridSize / 4, 3 * gridSize / 4)
                val y = Random.nextInt(gridSize / 4, 3 * gridSize / 4)
                fluidSim.addForce(
                    x, y,
                    Random.nextFloat() * 5f - 2.5f,
                    Random.nextFloat() * 5f - 2.5f
                )
            }

            // Add smooth density blobs
            for (i in 0 until 6) {
                // Place fewer, more spread out density points
                val x = gridSize / 2 + (Random.nextFloat() * 0.6f - 0.3f).toInt() * gridSize
                val y = gridSize / 2 + (Random.nextFloat() * 0.6f - 0.3f).toInt() * gridSize

                // Add density with wider spread and lower amount
                fluidSim.addSmoothDensity(x, y, 30f, 6)
            }

            // Run a few simulation steps to smooth things out before display
            repeat(15) {
                fluidSim.step()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchPosition = offset
                            previousTouchPosition = offset
                        },
                        onDrag = { change, _ ->
                            touchPosition = change.position
                            previousTouchPosition?.let { prev ->
                                touchVelocity = change.position - prev
                            }
                            previousTouchPosition = change.position
                        },
                        onDragEnd = {
                            touchPosition = null
                            previousTouchPosition = null
                            touchVelocity = Offset.Zero
                        },
                        onDragCancel = {
                            touchPosition = null
                            previousTouchPosition = null
                            touchVelocity = Offset.Zero
                        }
                    )
                }
        ) {
            canvasSize = size

            // Increment frame counter
            frameCount++

            // Apply touch forces and update simulation
            touchPosition?.let { touch ->
                val gridX = (touch.x / size.width * gridSize).toInt().coerceIn(1, gridSize - 2)
                val gridY = (touch.y / size.height * gridSize).toInt().coerceIn(1, gridSize - 2)

                // Add force based on touch velocity
                fluidSim.addForce(
                    gridX, gridY,
                    touchVelocity.x * FORCE_MULTIPLIER / size.width,
                    touchVelocity.y * FORCE_MULTIPLIER / size.height
                )

                // Add density (color) at touch point
                fluidSim.addDensity(gridX, gridY, DENSITY_MULTIPLIER)
            }

            // Always add ambient forces to keep fluid moving
            val time = frameCount * 0.01f

            // Add a continuous circular flow
            val centerX = gridSize / 2
            val centerY = gridSize / 2
            val radius = gridSize / 4

            // Circular flow
            val angle = time * 0.2f
            val flowX = centerX + (radius * cos(angle)).toInt()
            val flowY = centerY + (radius * sin(angle)).toInt()

            fluidSim.addForce(
                flowX, flowY,
                cos(angle + PI.toFloat() / 2) * AMBIENT_FLOW,
                sin(angle + PI.toFloat() / 2) * AMBIENT_FLOW
            )

            // Add random forces to create more interesting flow
            for (i in 0 until 3) {
                val x = Random.nextInt(1, gridSize - 1)
                val y = Random.nextInt(1, gridSize - 1)
                fluidSim.addForce(
                    x, y,
                    (Random.nextFloat() - 0.5f) * AMBIENT_FLOW * 2f,
                    (Random.nextFloat() - 0.5f) * AMBIENT_FLOW * 2f
                )
            }

            // Periodically add new density to keep the simulation visible
            if (frameCount % AUTO_ADD_INTERVAL == 0) {
                val x = Random.nextInt(gridSize / 4, 3 * gridSize / 4)
                val y = Random.nextInt(gridSize / 4, 3 * gridSize / 4)
                fluidSim.addDensity(x, y, 25f) // Smaller amount for smaller particles
            }

            fluidSim.step()
            fluidSim.render(this, size)
        }

        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameNanos {}
            }
        }

        // Back button
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            containerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        // Palette switcher button
        FloatingActionButton(
            onClick = {
                // Cycle through available color palettes
                currentPalette = when(currentPalette) {
                    ColorPalette.OCEAN -> ColorPalette.RAINBOW
                    ColorPalette.RAINBOW -> ColorPalette.FIRE
                    ColorPalette.FIRE -> ColorPalette.COOL
                    ColorPalette.COOL -> ColorPalette.MONO
                    ColorPalette.MONO -> ColorPalette.OCEAN
                }
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            containerColor = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Palette, contentDescription = "Change Color Palette")
        }
    }
}

// Fluid simulation class implementing a grid-based approach
class FluidSimulator(private val gridSize: Int) {
    // Simulation arrays
    private var s = Array(gridSize) { FloatArray(gridSize) { 0f } } // Density
    private var density = Array(gridSize) { FloatArray(gridSize) { 0f } } // New density

    private var vx = Array(gridSize) { FloatArray(gridSize) { 0f } } // Velocity X
    private var vy = Array(gridSize) { FloatArray(gridSize) { 0f } } // Velocity Y

    private var vx0 = Array(gridSize) { FloatArray(gridSize) { 0f } } // Previous velocity X
    private var vy0 = Array(gridSize) { FloatArray(gridSize) { 0f } } // Previous velocity Y

    // Default color palette
    private var colorPalette = arrayOf(
        Color(0xFF072448), // Deep blue
        Color(0xFF054a91), // Medium blue
        Color(0xFF3e7cb1), // Light blue
        Color(0xFF81a4cd), // Pale blue
        Color(0xFF54d2d2), // Teal
        Color(0xFFffcb00), // Yellow
        Color(0xFFf8aa4b), // Orange
        Color(0xFFff6150)  // Red
    )

    // Set color palette
    fun setPalette(palette: Array<Color>) {
        colorPalette = palette
    }

    // Add density with wider spread for smoother initialization
    fun addSmoothDensity(x: Int, y: Int, amount: Float, radius: Int) {
        val safeX = x.coerceIn(radius, gridSize - radius - 1)
        val safeY = y.coerceIn(radius, gridSize - radius - 1)

        // Add density in a wide area with smooth gaussian falloff
        for (i in -radius..radius) {
            for (j in -radius..radius) {
                val nx = safeX + i
                val ny = safeY + j
                if (nx in 1 until gridSize - 1 && ny in 1 until gridSize - 1) {
                    val distance = sqrt(i*i + j*j.toFloat())
                    val factor = exp(-distance * distance / (radius * 1.5f)) // Smoother, wider gaussian
                    s[nx][ny] += amount * factor
                }
            }
        }
    }

    // Reset simulation
    fun reset() {
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                s[i][j] = 0f
                density[i][j] = 0f
                vx[i][j] = 0f
                vy[i][j] = 0f
                vx0[i][j] = 0f
                vy0[i][j] = 0f
            }
        }
    }

    // Add density at a point
    fun addDensity(x: Int, y: Int, amount: Float) {
        val safeX = x.coerceIn(1, gridSize - 2)
        val safeY = y.coerceIn(1, gridSize - 2)

        // Add density in a small area (with gaussian-like falloff)
        for (i in -3..3) {
            for (j in -3..3) {
                val nx = safeX + i
                val ny = safeY + j
                if (nx in 1 until gridSize - 1 && ny in 1 until gridSize - 1) {
                    val distance = sqrt(i*i + j*j.toFloat())
                    val factor = exp(-distance * distance / 8) // Gaussian falloff
                    s[nx][ny] += amount * factor
                }
            }
        }
    }

    // Add velocity at a point
    fun addForce(x: Int, y: Int, amountX: Float, amountY: Float) {
        val safeX = x.coerceIn(1, gridSize - 2)
        val safeY = y.coerceIn(1, gridSize - 2)

        // Add velocity in a small area (with gaussian-like falloff)
        for (i in -3..3) {
            for (j in -3..3) {
                val nx = safeX + i
                val ny = safeY + j
                if (nx in 1 until gridSize - 1 && ny in 1 until gridSize - 1) {
                    val distance = sqrt(i*i + j*j.toFloat())
                    val factor = exp(-distance * distance / 8) // Gaussian falloff
                    vx[nx][ny] += amountX * factor
                    vy[nx][ny] += amountY * factor
                }
            }
        }
    }

    // Main simulation step
    fun step() {
        diffuse(1, vx0, vx, VISCOSITY)
        diffuse(2, vy0, vy, VISCOSITY)

        project(vx0, vy0, vx, vy)

        advect(1, vx, vx0, vx0, vy0)
        advect(2, vy, vy0, vx0, vy0)

        project(vx, vy, vx0, vy0)

        diffuse(0, density, s, DIFFUSION)
        advect(0, s, density, vx, vy)

        // Apply damping to velocity field and decay to density
        for (i in 0 until gridSize) {
            for (j in 0 until gridSize) {
                vx[i][j] *= DAMPING
                vy[i][j] *= DAMPING
                s[i][j] *= DENSITY_DECAY
            }
        }
    }

    // Diffuse the fluid - handles how quantities spread over time
    private fun diffuse(b: Int, x: Array<FloatArray>, x0: Array<FloatArray>, diff: Float) {
        val a = DT * diff * (gridSize - 2) * (gridSize - 2)
        linearSolve(b, x, x0, a, 1 + 4 * a)
    }

    // Solves linear system for diffusion and pressure
    private fun linearSolve(b: Int, x: Array<FloatArray>, x0: Array<FloatArray>, a: Float, c: Float) {
        val cRecip = 1.0f / c
        for (k in 0 until ITERATIONS) {
            for (i in 1 until gridSize - 1) {
                for (j in 1 until gridSize - 1) {
                    x[i][j] = (x0[i][j] + a * (x[i+1][j] + x[i-1][j] + x[i][j+1] + x[i][j-1])) * cRecip
                }
            }
            setBoundary(b, x)
        }
    }

    // Project step - makes the velocity field mass-conserving
    private fun project(velocX: Array<FloatArray>, velocY: Array<FloatArray>, p: Array<FloatArray>, div: Array<FloatArray>) {
        for (i in 1 until gridSize - 1) {
            for (j in 1 until gridSize - 1) {
                div[i][j] = -0.5f * (
                        velocX[i+1][j] - velocX[i-1][j] +
                                velocY[i][j+1] - velocY[i][j-1]
                        ) / gridSize
                p[i][j] = 0f
            }
        }
        setBoundary(0, div)
        setBoundary(0, p)

        linearSolve(0, p, div, 1f, 4f)

        for (i in 1 until gridSize - 1) {
            for (j in 1 until gridSize - 1) {
                velocX[i][j] -= 0.5f * (p[i+1][j] - p[i-1][j]) * gridSize
                velocY[i][j] -= 0.5f * (p[i][j+1] - p[i][j-1]) * gridSize
            }
        }

        setBoundary(1, velocX)
        setBoundary(2, velocY)
    }

    // Advection - moves quantities along the velocity field
    private fun advect(b: Int, d: Array<FloatArray>, d0: Array<FloatArray>, velocX: Array<FloatArray>, velocY: Array<FloatArray>) {
        var i0: Int
        var i1: Int
        var j0: Int
        var j1: Int

        val dtx = DT * (gridSize - 2)
        val dty = DT * (gridSize - 2)

        var s0: Float
        var s1: Float
        var t0: Float
        var t1: Float

        var x: Float
        var y: Float

        for (i in 1 until gridSize - 1) {
            for (j in 1 until gridSize - 1) {
                val tmp1 = dtx * velocX[i][j]
                val tmp2 = dty * velocY[i][j]

                x = i - tmp1
                y = j - tmp2

                x = max(0.5f, min(gridSize - 1.5f, x))
                i0 = x.toInt()
                i1 = i0 + 1

                y = max(0.5f, min(gridSize - 1.5f, y))
                j0 = y.toInt()
                j1 = j0 + 1

                s1 = x - i0
                s0 = 1 - s1
                t1 = y - j0
                t0 = 1 - t1

                d[i][j] = s0 * (t0 * d0[i0][j0] + t1 * d0[i0][j1]) +
                        s1 * (t0 * d0[i1][j0] + t1 * d0[i1][j1])
            }
        }

        setBoundary(b, d)
    }

    // Set boundary conditions
    private fun setBoundary(b: Int, x: Array<FloatArray>) {
        // First set the vertical walls
        for (i in 1 until gridSize - 1) {
            x[0][i] = if (b == 1) -x[1][i] else x[1][i]
            x[gridSize - 1][i] = if (b == 1) -x[gridSize - 2][i] else x[gridSize - 2][i]
        }

        // Then the horizontal walls
        for (i in 1 until gridSize - 1) {
            x[i][0] = if (b == 2) -x[i][1] else x[i][1]
            x[i][gridSize - 1] = if (b == 2) -x[i][gridSize - 2] else x[i][gridSize - 2]
        }

        // Finally the corners
        x[0][0] = 0.5f * (x[1][0] + x[0][1])
        x[0][gridSize - 1] = 0.5f * (x[1][gridSize - 1] + x[0][gridSize - 2])
        x[gridSize - 1][0] = 0.5f * (x[gridSize - 2][0] + x[gridSize - 1][1])
        x[gridSize - 1][gridSize - 1] = 0.5f * (x[gridSize - 2][gridSize - 1] + x[gridSize - 1][gridSize - 2])
    }

    // Render the fluid simulation
    fun render(drawScope: DrawScope, canvasSize: Size) {
        with(drawScope) {
            val cellWidth = canvasSize.width / gridSize
            val cellHeight = canvasSize.height / gridSize

            for (i in 0 until gridSize) {
                for (j in 0 until gridSize) {
                    val density = s[i][j]
                    if (density > 0.001f) { // Lower threshold for visibility
                        // Map density to color using a better distribution
                        val normalizedDensity = min(density / 100f, 1f)
                        val colorIndex = (normalizedDensity * (colorPalette.size - 1)).toInt()
                        val color = when {
                            colorIndex < 0 -> colorPalette[0]
                            colorIndex >= colorPalette.size -> colorPalette.last()
                            else -> {
                                // Interpolate between colors for smoother transitions
                                val fraction = (normalizedDensity * (colorPalette.size - 1)) - colorIndex
                                if (colorIndex < colorPalette.size - 1 && fraction > 0) {
                                    lerp(colorPalette[colorIndex], colorPalette[colorIndex + 1], fraction)
                                } else {
                                    colorPalette[colorIndex]
                                }
                            }
                        }

                        // Calculate velocity magnitude for additional visual effect
                        val velocityMag = sqrt(vx[i][j] * vx[i][j] + vy[i][j] * vy[i][j])

                        // Lower alpha for smoother appearance
                        val alpha = min(0.75f, normalizedDensity * 0.8f + velocityMag / 40f)

                        drawRect(
                            color = color.copy(alpha = alpha),
                            topLeft = Offset(i * cellWidth, j * cellHeight),
                            size = Size(cellWidth, cellHeight)
                        )
                    }
                }
            }
        }
    }

    // Helper function to interpolate between colors
    private fun lerp(c1: Color, c2: Color, t: Float): Color {
        return Color(
            red = c1.red + (c2.red - c1.red) * t,
            green = c1.green + (c2.green - c1.green) * t,
            blue = c1.blue + (c2.blue - c1.blue) * t,
            alpha = c1.alpha + (c2.alpha - c1.alpha) * t
        )
    }
}