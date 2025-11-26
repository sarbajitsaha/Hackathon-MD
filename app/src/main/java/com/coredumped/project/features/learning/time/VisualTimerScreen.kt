package com.coredumped.project.features.learning.time

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.Ringtone
import android.net.Uri
import android.util.Log
import kotlin.math.min as mathMin
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.min
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import android.widget.VideoView
import android.widget.MediaController

@Composable
fun VisualTimerScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    // --- Timer State ---
    var selectedMinutes by remember { mutableStateOf(5) }
    var totalSeconds by remember { mutableStateOf(selectedMinutes * 60) }
    var remainingSeconds by remember { mutableStateOf(totalSeconds) }
    var isRunning by remember { mutableStateOf(false) }

    // --- Alarm State ---
    var alarmRingtone by remember { mutableStateOf<Ringtone?>(null) }

    // --- Video State ---
    var showVideo by remember { mutableStateOf(false) }
    var videoView by remember { mutableStateOf<VideoView?>(null) }

    // --- Animations ---

    // 1. Smooth Sweep
    val elapsedSeconds = (totalSeconds - remainingSeconds).toFloat()
    val animatedElapsedSeconds by animateFloatAsState(
        targetValue = elapsedSeconds,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timer_sweep"
    )

    // 2. Pulsing Effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 0.85f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // 3. Confetti System
    var showConfetti by remember { mutableStateOf(false) }
    val confettiParticles = remember { List(100) { ConfettiParticle() } }

    // Function to setup and play video
    fun setupVideo(videoView: VideoView) {
        try {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.rabbit_eating_carrot}")
            videoView.setVideoURI(videoUri)

            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true // Loop the video
                videoView.start()
                Log.d("TimerVideo", "Video started playing in loop")
            }

            videoView.setOnErrorListener { _, what, extra ->
                Log.e("TimerVideo", "Video error: what=$what, extra=$extra")
                false
            }

        } catch (e: Exception) {
            Log.e("TimerVideo", "Error setting up video: ${e.message}", e)
        }
    }

    // Function to stop video
    fun stopVideo() {
        try {
            videoView?.let { vv ->
                if (vv.isPlaying) {
                    vv.stopPlayback()
                    Log.d("TimerVideo", "Video stopped")
                }
            }
            showVideo = false
        } catch (e: Exception) {
            Log.e("TimerVideo", "Error stopping video: ${e.message}", e)
        }
    }

    // Timer Logic with Alarm and Video
    LaunchedEffect(key1 = isRunning, key2 = remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        } else if (remainingSeconds == 0 && isRunning) {
            isRunning = false
            showConfetti = true
            showVideo = true // Show video when timer finishes

            // Play Alarm Sound for 3 seconds
            try {
                // Get the default notification/alarm tone
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                val ringtone = RingtoneManager.getRingtone(context, alarmUri)
                alarmRingtone = ringtone

                if (ringtone != null) {
                    ringtone.play()
                    Log.d("TimerAlarm", "Alarm started playing")

                    // Stop the alarm after 3 seconds
                    delay(3000)
                    if (ringtone.isPlaying) {
                        ringtone.stop()
                        Log.d("TimerAlarm", "Alarm stopped after 3 seconds")
                    }
                } else {
                    Log.e("TimerAlarm", "Could not get ringtone")
                }
            } catch (e: Exception) {
                Log.e("TimerAlarm", "Error playing alarm: ${e.message}", e)
            }
        }
    }

    // Cleanup alarm and video when component is disposed
    DisposableEffect(Unit) {
        onDispose {
            try {
                alarmRingtone?.let { ringtone ->
                    if (ringtone.isPlaying) {
                        ringtone.stop()
                    }
                }
                stopVideo()
            } catch (e: Exception) {
                Log.e("TimerCleanup", "Error during cleanup: ${e.message}", e)
            }
        }
    }

    // Function to stop alarm manually (if needed)
    fun stopAlarm() {
        try {
            alarmRingtone?.let { ringtone ->
                if (ringtone.isPlaying) {
                    ringtone.stop()
                    Log.d("TimerAlarm", "Alarm stopped manually")
                }
            }
        } catch (e: Exception) {
            Log.e("TimerAlarm", "Error stopping alarm manually: ${e.message}", e)
        }
    }

    if (showConfetti) {
        LaunchedEffect(Unit) {
            while (true) {
                confettiParticles.forEach { it.update() }
                delay(16)
            }
        }
        LaunchedEffect(Unit) {
            delay(4000)
            showConfetti = false
            confettiParticles.forEach { it.reset() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1.0f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.3f))
        )

        // Main Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- LEFT SIDE: ANIMATED TIMER ---
            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    // Timer Canvas (background layer)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val canvasRadius = size.minDimension / 2

                        // 1. White Background
                        drawCircle(color = Color.White, radius = canvasRadius, center = center)

                        // 2. Red Time Arc (only show if not showing video)
                        if (!showVideo) {
                            val progressFraction = if (totalSeconds > 0) animatedElapsedSeconds / totalSeconds else 0f
                            val sweepAngle = progressFraction * 360f

                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = Color(0xFFE53935).copy(alpha = pulseAlpha),
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    topLeft = Offset(center.x - canvasRadius, center.y - canvasRadius),
                                    size = Size(canvasRadius * 2, canvasRadius * 2)
                                )
                            }
                        }

                        // 3. Gray Border
                        drawCircle(
                            color = Color(0xFFDDDDDD),
                            radius = canvasRadius,
                            center = center,
                            style = Stroke(width = 4f)
                        )

                        // 4. Ticks
                        val tickLength = canvasRadius * 0.08f
                        for (i in 0 until 12) {
                            val angle = -90 + (i * 30)
                            val angleRad = Math.toRadians(angle.toDouble())
                            val startX = center.x + (canvasRadius - tickLength) * cos(angleRad).toFloat()
                            val startY = center.y + (canvasRadius - tickLength) * sin(angleRad).toFloat()
                            val endX = center.x + canvasRadius * cos(angleRad).toFloat()
                            val endY = center.y + canvasRadius * sin(angleRad).toFloat()

                            drawLine(
                                color = if (i % 3 == 0) Color(0xFF444444) else Color(0xFFAAAAAA),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 3 == 0) 6f else 3f
                            )
                        }
                    }

                    // Video Player (overlaid in the center when timer finishes)
                    if (showVideo) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val boxSize = maxWidth
                            val videoSize = (boxSize * 0.8f) // Make video slightly smaller than the circle

                            AndroidView(
                                factory = { context ->
                                    VideoView(context).apply {
                                        videoView = this
                                        setupVideo(this)
                                    }
                                },
                                modifier = Modifier
                                    .size(videoSize)
                                    .align(Alignment.Center)
                                    .clip(CircleShape), // Clip to circle shape
                                update = { vv ->
                                    if (showVideo && !vv.isPlaying) {
                                        setupVideo(vv)
                                    }
                                }
                            )
                        }
                    }

                    // --- EMOJI ANIMATIONS (only show when video is not playing) ---
                    if (!showVideo) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val boxSize = maxWidth
                            val radius = boxSize / 2

                            // 1. The Carrot
                            Text(
                                text = "🥕",
                                fontSize = 32.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = -radius + 24.dp)
                            )

                            val progressFraction = if (totalSeconds > 0) animatedElapsedSeconds / totalSeconds else 0f
                            val sweepAngle = progressFraction * 360f
                            val orbitRadius = radius - 24.dp

                            val effectiveAngle = if (remainingSeconds == totalSeconds) -15f else sweepAngle
                            val rabbitAngleRad = Math.toRadians((-90 + effectiveAngle).toDouble())

                            val rabbitX = orbitRadius * cos(rabbitAngleRad).toFloat()
                            val rabbitY = orbitRadius * sin(rabbitAngleRad).toFloat()

                            Text(
                                text = "🐰",
                                fontSize = 32.sp,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(x = rabbitX, y = rabbitY)
                            )
                        }
                    }
                }
            }

            // --- RIGHT SIDE: CONTROLS ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. Time Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60

                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    // Show "Great Job" only when finished
                    if (remainingSeconds == 0 && totalSeconds > 0) {
                        Text(
                            text = "Great Job!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                // 2. Controls Logic - Dynamic Switching
                if (remainingSeconds == 0 && totalSeconds > 0) {
                    // --- FINISHED STATE ---
                    Button(
                        onClick = {
                            stopAlarm() // Stop alarm if still playing
                            stopVideo() // Stop video
                            isRunning = false
                            remainingSeconds = totalSeconds
                            showConfetti = false
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(72.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(36.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("RESET", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // --- RUNNING / SETUP STATE ---

                    // Adjustment Controls (Only when NOT running)
                    if (!isRunning) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // +/- Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (selectedMinutes > 1) {
                                            selectedMinutes--
                                            totalSeconds = selectedMinutes * 60
                                            remainingSeconds = totalSeconds
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .shadow(2.dp, CircleShape)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Icon(Icons.Default.Remove, "Decrease", Modifier.size(24.dp), Color(0xFF424242))
                                }

                                Text(
                                    text = "$selectedMinutes min",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )

                                IconButton(
                                    onClick = {
                                        if (selectedMinutes < 60) {
                                            selectedMinutes++
                                            totalSeconds = selectedMinutes * 60
                                            remainingSeconds = totalSeconds
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .shadow(2.dp, CircleShape)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Icon(Icons.Default.Add, "Increase", Modifier.size(24.dp), Color(0xFF424242))
                                }
                            }

                            // Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf(1, 5, 15, 30, 60).forEach { minutes ->
                                    Button(
                                        onClick = {
                                            selectedMinutes = minutes
                                            totalSeconds = minutes * 60
                                            remainingSeconds = totalSeconds
                                        },
                                        modifier = Modifier.size(48.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selectedMinutes == minutes) Color(0xFFE53935) else Color.White
                                        ),
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(0.dp),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    ) {
                                        Text(
                                            text = "$minutes",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedMinutes == minutes) Color.White else Color(0xFF424242)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Start/Pause Button
                    Button(
                        onClick = { if (remainingSeconds > 0) isRunning = !isRunning },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) Color(0xFFFFA726) else Color(0xFF43A047),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        ),
                        shape = RoundedCornerShape(32.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        enabled = remainingSeconds > 0
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "PAUSE" else "START",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Small Reset Button (Only if running or modified)
                    if (isRunning || remainingSeconds != totalSeconds) {
                        TextButton(
                            onClick = {
                                stopAlarm() // Stop alarm if playing
                                stopVideo() // Stop video if playing
                                isRunning = false
                                remainingSeconds = totalSeconds
                                showConfetti = false
                            },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("RESET", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF757575))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }

        // Confetti
        if (showConfetti) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                confettiParticles.forEach { particle ->
                    drawCircle(color = particle.color, radius = particle.size, center = Offset(particle.x, particle.y))
                }
            }
        }

        // Back Button
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
                    stopAlarm() // Stop alarm when navigating back
                    stopVideo() // Stop video when navigating back
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
    }
}

class ConfettiParticle {
    var x by mutableFloatStateOf(0f)
    var y by mutableFloatStateOf(0f)
    var velocityY by mutableFloatStateOf(0f)
    var velocityX by mutableFloatStateOf(0f)
    var color by mutableStateOf(Color.Red)
    var size by mutableFloatStateOf(0f)

    init { reset() }

    fun reset() {
        x = Random.nextFloat() * 2000f
        y = Random.nextFloat() * -500f
        velocityY = Random.nextFloat() * 10f + 5f
        velocityX = Random.nextFloat() * 4f - 2f
        size = Random.nextFloat() * 15f + 10f

        val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFEB3B), Color(0xFFFF9800))
        color = colors[Random.nextInt(colors.size)]
    }

    fun update() {
        y += velocityY
        x += velocityX
        velocityY += 0.5f
    }
}
