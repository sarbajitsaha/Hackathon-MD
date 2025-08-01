package com.coredumped.project.calm

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.coredumped.project.R
import kotlinx.coroutines.delay

@Composable
fun FluidSimulationScreen(navController: NavController) {
    val showTutorial = remember { mutableStateOf(true) }
    // A state to hold the WebView instance so it can be passed to the tutorial
    var webView by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current

        // WebView for the fluid simulation
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                            Log.d(
                                "WebViewConsole",
                                "${message?.message()} -- From line ${message?.lineNumber()} of ${message?.sourceId()}"
                            )
                            return true
                        }
                    }
                    loadUrl("file:///android_asset/index.html")
                }
            },
            // Get the WebView instance once it's created
            update = { webView = it }
        )

        // Show the tutorial animation on top of the WebView
        if (showTutorial.value) {
            // Pass the WebView instance to the tutorial
            FluidDragTutorial(
                webView = webView,
                onComplete = { showTutorial.value = false }
            )
        }

        // Colorful back button, aligned to the top-left corner
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
                contentDescription = stringResource(R.string.back_button),
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * A tutorial animation that simulates a finger dragging across the screen
 * and sends corresponding touch events to the provided WebView.
 *
 * @param webView The WebView instance to send touch events to.
 * @param onComplete A callback to be invoked when the animation finishes.
 */
@Composable
private fun FluidDragTutorial(webView: WebView?, onComplete: () -> Unit) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val fingerX = remember { Animatable(0f) }
    val fingerY = remember { Animatable(screenHeightPx / 2f) }
    val fingerAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "finger_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "finger_pulse_scale"
    )

    // The main animation sequence
    LaunchedEffect(webView) { // Relaunch if the webView instance changes
        // Do nothing until the WebView is ready
        if (webView == null) return@LaunchedEffect

        val startX = screenWidthPx * 0.15f
        val endX = screenWidthPx * 0.85f
        val yPos = screenHeightPx / 2f

        // Helper function to dispatch a pointer event into the WebView
        fun dispatchPointerEvent(type: String, x: Float, y: Float) {
            val script = "window.dispatchEvent(new PointerEvent('$type', { clientX: $x, clientY: $y, bubbles: true }));"
            // Post to the WebView's handler to run on the UI thread
            webView.post { webView.evaluateJavascript(script, null) }
        }

        fingerX.snapTo(startX)
        fingerY.snapTo(yPos)
        delay(1000)

        fingerAlpha.animateTo(1f, animationSpec = tween(500))
        delay(300)

        // --- Animate swipe from left to right ---
        dispatchPointerEvent("pointerdown", startX, yPos)
        // The block in animateTo gets called on every frame of the animation
        fingerX.animateTo(endX, animationSpec = tween(durationMillis = 1500)) {
            // `this.value` is the current X position of the finger
            dispatchPointerEvent("pointermove", this.value, yPos)
        }
        dispatchPointerEvent("pointerup", endX, yPos)
        delay(500)

        // --- Animate swipe from right to left ---
        dispatchPointerEvent("pointerdown", endX, yPos)
        fingerX.animateTo(startX, animationSpec = tween(durationMillis = 1500)) {
            dispatchPointerEvent("pointermove", this.value, yPos)
        }
        dispatchPointerEvent("pointerup", startX, yPos)
        delay(500)

        fingerAlpha.animateTo(0f, animationSpec = tween(500))
        onComplete()
    }

    // Composable for the finger icon itself
    Box(
        modifier = Modifier
            .offset { IntOffset(fingerX.value.toInt(), fingerY.value.toInt()) }
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
                alpha = fingerAlpha.value
                translationX = -size.width / 2
                translationY = -size.height / 2
            }
    ) {
        val fingerIconSize = 56.dp
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier
                .size(fingerIconSize)
                .offset(2.dp, 2.dp)
        )
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = stringResource(R.string.tutorial_drag_finger),
            tint = Color.White,
            modifier = Modifier.size(fingerIconSize)
        )
    }
}