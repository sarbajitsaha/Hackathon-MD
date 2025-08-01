package com.coredumped.project.calm

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
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
    var webView by remember { mutableStateOf<WebView?>(null) }
    // State to track if the JS inside the WebView is ready
    var isWebViewReady by remember { mutableStateOf(false) }

    // This class acts as a bridge between JavaScript and Kotlin
    class JsBridge(private val onReady: () -> Unit) {
        @JavascriptInterface
        fun onPageReady() {
            Log.d("FluidSim", "JavaScript has called onPageReady.")
            onReady()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val context = LocalContext.current

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.allowFileAccessFromFileURLs = true

                    // Add the bridge that JS can call
                    addJavascriptInterface(JsBridge { isWebViewReady = true }, "AndroidBridge")

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
            update = { wv ->
                webView = wv
            }
        )

        // Show the tutorial only if it's enabled AND the webview has reported it's ready
        if (showTutorial.value && isWebViewReady) {
            FluidDragTutorial(
                webView = webView,
                onComplete = { showTutorial.value = false }
            )
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
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

    LaunchedEffect(webView) {
        if (webView == null) return@LaunchedEffect

        val startX = screenWidthPx * 0.25f
        val endX = screenWidthPx * 0.75f
        val yPos = screenHeightPx / 2f

        fun dispatchMouseEvent(type: String, x: Float, y: Float) {
            // Target the 'canvas' for down/move, but 'window' for up, to match script.js
            val target = if (type == "mouseup") "window" else "document.getElementsByTagName('canvas')[0]"
            val script = """
                (function() {
                    const el = $target;
                    if (el) {
                        const event = new MouseEvent('$type', {
                            clientX: $x,
                            clientY: $y,
                            bubbles: true,
                            cancelable: true
                        });
                        el.dispatchEvent(event);
                    }
                })();
            """.trimIndent()
            webView.post { webView.evaluateJavascript(script, null) }
        }

        fingerX.snapTo(startX)
        fingerY.snapTo(yPos)

        // Appear and wait
        fingerAlpha.animateTo(1f, animationSpec = tween(100))
        delay(1000)

        // --- Animate swipe from left to right ---
        dispatchMouseEvent("mousedown", startX, yPos)
        fingerX.animateTo(endX, animationSpec = tween(durationMillis = 2500)) {
            dispatchMouseEvent("mousemove", this.value, yPos)
        }
        dispatchMouseEvent("mouseup", endX, yPos)
        delay(100)

        // --- Animate swipe from right to left ---
        dispatchMouseEvent("mousedown", endX, yPos)
        fingerX.animateTo(startX, animationSpec = tween(durationMillis = 3500)) {
            dispatchMouseEvent("mousemove", this.value, yPos)
        }
        dispatchMouseEvent("mouseup", startX, yPos)
        delay(100)

        // Fade out and complete
        fingerAlpha.animateTo(0f, animationSpec = tween(100))
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