package com.coredumped.project.ui

import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.min as mathMin

private const val TAG = "AnalogClockLearning"

// Teaching moments in 24-hour format (hour, minute)
data class TeachingMoment(val hour: Int, val minute: Int) {
    fun toMinuteOfDay(): Int = hour * 60 + minute
    fun formatTime(): String {
        // Special case for 24:00 representing midnight at the end of the sequence
        if (hour == 24) return "12:00 AM"

        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val amPm = if (hour < 12) "AM" else "PM"
        return if (minute == 0) {
            "$displayHour:00 $amPm"
        } else {
            "$displayHour:%02d $amPm".format(minute)
        }
    }
}

private val teachingMoments = listOf(
    TeachingMoment(7, 0),    // 7:00 AM
    TeachingMoment(9, 0),    // 9:00 AM
    TeachingMoment(12, 0),   // 12:00 PM
    TeachingMoment(12, 30), // 12:30 PM
    TeachingMoment(15, 0),   // 3:00 PM
    TeachingMoment(18, 0),   // 6:00 PM
    TeachingMoment(20, 0),   // 8:00 PM
    TeachingMoment(22, 0),   // 10:00 PM
    TeachingMoment(24, 0)    // 12:00 AM (Midnight)
)

// Helper to blend two colors
fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * clampedFraction,
        green = start.green + (end.green - start.green) * clampedFraction,
        blue = start.blue + (end.blue - start.blue) * clampedFraction,
        alpha = start.alpha + (end.alpha - start.alpha) * clampedFraction
    )
}

// Helper to blend a list of gradient colors
fun lerpGradient(start: List<Color>, end: List<Color>, fraction: Float): List<Color> {
    return start.zip(end) { s, e -> lerpColor(s, e, fraction) }
}

@Composable
fun AnalogClockLearningScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Time state (starts at 7:00 AM = 420 minutes)
    var currentMinuteOfDay by remember { mutableStateOf(7 * 60f) }
    var isAnimating by remember { mutableStateOf(true) }
    var isPaused by remember { mutableStateOf(false) }
    var currentTeachingIndex by remember { mutableIntStateOf(0) }

    // Audio player
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Calculate current hours and minutes for logic
    val currentHourInt = (currentMinuteOfDay / 60f).toInt() % 24

    // Precise float for smooth calculations
    // Note: We use % 24f for display logic, but for 24:00 teaching moment we allow it to reach 24
    val currentHourFloat = (currentMinuteOfDay / 60f)

    // Animated rotations for smooth hand movement
    val minuteRotation by animateFloatAsState(
        targetValue = (currentMinuteOfDay % 60f * 6f),
        animationSpec = tween(durationMillis = 0),
        label = "minute_rotation"
    )

    val hourRotation by animateFloatAsState(
        targetValue = (currentMinuteOfDay * 0.5f) % 360f,
        animationSpec = tween(durationMillis = 0),
        label = "hour_rotation"
    )

    // --- SMOOTH GRADIENT LOGIC ---
    // Define base palettes
    val nightColors = listOf(Color(0xFF1A237E), Color(0xFF303F9F), Color(0xFF512DA8))
    val dawnColors = listOf(Color(0xFFFF6F61), Color(0xFFFFB74D), Color(0xFF81D4FA))
    val dayColors = listOf(Color(0xFF4FC3F7), Color(0xFF81D4FA), Color(0xFFB3E5FC))
    val duskColors = listOf(Color(0xFFFF6F61), Color(0xFFBA68C8), Color(0xFF5E35B1))

    // Calculate background based on continuous time
    // Normalized hour for cycle (0-24)
    val cycleHour = currentHourFloat % 24f

    val backgroundGradient = remember(cycleHour) {
        when {
            cycleHour < 5f -> nightColors // Late night
            cycleHour < 6f -> lerpGradient(nightColors, dawnColors, cycleHour - 5f) // Night to Dawn
            cycleHour < 7f -> lerpGradient(dawnColors, dayColors, cycleHour - 6f) // Dawn to Day
            cycleHour < 17f -> dayColors // Day
            cycleHour < 18.5f -> lerpGradient(dayColors, duskColors, (cycleHour - 17f) / 1.5f) // Day to Dusk
            cycleHour < 20f -> lerpGradient(duskColors, nightColors, (cycleHour - 18.5f) / 1.5f) // Dusk to Night
            else -> nightColors // Night
        }
    }

    // Calculate sun/moon position (0 to 180 degrees)
    val sunMoonAngle = when {
        cycleHour in 6f..18f -> {
            // Daytime: sun arc from 6am (0) to 6pm (180)
            ((cycleHour - 6f) / 12f) * 180f
        }
        else -> {
            // Nighttime: moon arc from 6pm (0) to 6am (180)
            val hoursSinceSunset = if (cycleHour >= 18f) (cycleHour - 18f) else (cycleHour + 6f)
            (hoursSinceSunset / 12f) * 180f
        }
    }

    val isSunVisible = cycleHour in 6f..18f

    // Initialize ExoPlayer
    DisposableEffect(context) {
        Log.d(TAG, "Initializing ExoPlayer")
        val player = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        Log.d(TAG, "Audio playback completed")
                    }
                }
            })
        }
        exoPlayer = player

        onDispose {
            Log.d(TAG, "Releasing ExoPlayer")
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    // Lifecycle observer to pause animation when app goes to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    isAnimating = false
                    exoPlayer?.pause()
                    Log.d(TAG, "App paused")
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (!isPaused) {
                        isAnimating = true
                    }
                    Log.d(TAG, "App resumed")
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Function to play audio
    fun playAudio() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            // Using placeholder audio from raw resources
            val uri = Uri.parse("android.resource://${context.packageName}/${R.raw.correct}")
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
            Log.d(TAG, "Playing audio for teaching moment")
        }
    }

    // Time animation logic - smooth continuous movement with consistent speed
            .background(
                brush = Brush.verticalGradient(
                    colors = backgroundGradient
                )
            )
    ) {
        // Sun/Moon and stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate sun/moon position on an arc
            val centerX = canvasWidth / 2f

            // Radius limited by height to keep sun on screen
            val arcRadius = (canvasWidth * 0.45f).coerceAtMost(canvasHeight * 0.5f)

            // Base horizon position
            val horizonY = canvasHeight * 0.65f

            // Map 0..180 angle to Radians for correct trajectory
            val angleRad = ((180f - sunMoonAngle) * (PI / 180f)).toFloat()

            val celestialX = centerX + arcRadius * cos(angleRad)
            val celestialY = horizonY - (arcRadius * sin(angleRad) * 0.8f)

            if (isSunVisible) {
                // Draw sun
                // Sun glow
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.3f),
                    radius = 80f,
                    center = Offset(celestialX, celestialY)
                )
                // Sun core
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 50f,
                    center = Offset(celestialX, celestialY)
                )
                // Sun rays
                val rayRotationOffset = (currentMinuteOfDay % 90f) * 4f

                for (i in 0..7) {
                    val rayAngle = (i * 45f + rayRotationOffset) * (PI / 180f).toFloat()
                    val startX = celestialX + 60f * cos(rayAngle)
                    val startY = celestialY + 60f * sin(rayAngle)
                    val endX = celestialX + 85f * cos(rayAngle)
                    val endY = celestialY + 85f * sin(rayAngle)

                    drawLine(
                        color = Color(0xFFFFEB3B),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            } else {
                // Draw moon
                drawCircle(
                    color = Color(0xFFF5F5F5).copy(alpha = 0.9f),
                    radius = 50f,
                    center = Offset(celestialX, celestialY)
                )
                // Moon crescent shadow
                drawCircle(
                    color = Color(0xFF1A237E).copy(alpha = 0.3f),
                    radius = 45f,
                    center = Offset(celestialX + 20f, celestialY - 5f)
                )

                // Draw stars (only at night)
                val starPositions = listOf(
                    Offset(canvasWidth * 0.15f, canvasHeight * 0.15f),
                    Offset(canvasWidth * 0.85f, canvasHeight * 0.2f),
                    Offset(canvasWidth * 0.25f, canvasHeight * 0.35f),
                    Offset(canvasWidth * 0.75f, canvasHeight * 0.4f),
                    Offset(canvasWidth * 0.1f, canvasHeight * 0.5f),
                    Offset(canvasWidth * 0.9f, canvasHeight * 0.55f),
                )

                for (star in starPositions) {
                    val starSize = 4f + (currentMinuteOfDay % 10f) * 0.3f
                    drawCircle(
                        color = Color.White,
                        radius = starSize,
                        center = star
                    )
                }
            }
        }

        // Custom Back Button
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val backButtonSize = mathMin(64f, screenWidth * 0.12f).dp

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(backButtonSize)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9500),
                            Color(0xFFFF2D55),
                            Color(0xFF5856D6)
                        )
                    )
                )
                .clickable {
                    // stopAlarm()
                    // stopVideo()
                    navController.popBackStack()
                }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(mathMin(32f, screenWidth * 0.06f).dp)
            )
        }

        // Main content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Analog Clock
            Box(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .fillMaxHeight(0.8f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val centerX = canvasWidth / 2f
                    val centerY = canvasHeight / 2f
                    val radius = (canvasWidth.coerceAtMost(canvasHeight) / 2f) * 0.8f

                    // Draw clock face background
                    drawCircle(
                        color = Color.White,
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )

                    // Draw clock border
                    drawCircle(
                        color = Color.Black,
                        radius = radius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 8f)
                    )

                    // Draw hour numbers
                    for (i in 1..12) {
                        val angle = (i * 30 - 90) * (PI / 180f).toFloat()
                        val x = centerX + (radius * 0.75f) * cos(angle)
                        val y = centerY + (radius * 0.75f) * sin(angle)

                        drawContext.canvas.nativeCanvas.drawText(
                            i.toString(),
                            x,
                            y + 15f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = 48f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }

                    // Draw hour markers (tick marks)
                    for (i in 0..59) {
                        val angle = (i * 6 - 90) * (PI / 180f).toFloat()
                        val isHourMark = i % 5 == 0
                        val startRadius = if (isHourMark) radius * 0.85f else radius * 0.9f
                        val endRadius = radius * 0.95f
                        val strokeWidth = if (isHourMark) 6f else 2f

                        val startX = centerX + startRadius * cos(angle)
                        val startY = centerY + startRadius * sin(angle)
                        val endX = centerX + endRadius * cos(angle)
                        val endY = centerY + endRadius * sin(angle)

                        drawLine(
                            color = Color.Black,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = strokeWidth
                        )
                    }

                    // Draw hour hand
                    val hourAngle = (hourRotation - 90) * (PI / 180f).toFloat()
                    val hourHandLength = radius * 0.5f
                    val hourHandEndX = centerX + hourHandLength * cos(hourAngle)
                    val hourHandEndY = centerY + hourHandLength * sin(hourAngle)

                    val hourStartOffsetX = centerX - (cos(hourAngle) * 10f)
                    val hourStartOffsetY = centerY - (sin(hourAngle) * 10f)

                    drawLine(
                        color = Color(0xFF1976D2), // Blue
                        start = Offset(hourStartOffsetX, hourStartOffsetY),
                        end = Offset(hourHandEndX, hourHandEndY),
                        strokeWidth = 16f,
                        cap = StrokeCap.Round
                    )

                    // Draw minute hand
                    val minuteAngle = (minuteRotation - 90) * (PI / 180f).toFloat()
                    val minuteHandLength = radius * 0.7f
                    val minuteHandEndX = centerX + minuteHandLength * cos(minuteAngle)
                    val minuteHandEndY = centerY + minuteHandLength * sin(minuteAngle)

                    val minuteStartOffsetX = centerX - (cos(minuteAngle) * 10f)
                    val minuteStartOffsetY = centerY - (sin(minuteAngle) * 10f)

                    drawLine(
                        color = Color(0xFFD32F2F), // Red
                        start = Offset(minuteStartOffsetX, minuteStartOffsetY),
                        end = Offset(minuteHandEndX, minuteHandEndY),
                        strokeWidth = 12f,
                        cap = StrokeCap.Round
                    )

                    // Draw center dot
                    drawCircle(
                        color = Color.Black,
                        radius = 20f,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            // Digital time display and controls
            if (isPaused) {
                Spacer(modifier = Modifier.width(32.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Digital time display
                    Text(
                        text = teachingMoments[currentTeachingIndex].formatTime(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Play Audio button
                    Button(
                        onClick = { playAudio() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(180.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play Audio",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play Audio",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next button
                    Button(
                        onClick = { handleNext() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(180.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}