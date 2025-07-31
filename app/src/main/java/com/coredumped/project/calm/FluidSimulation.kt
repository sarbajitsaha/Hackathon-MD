package com.coredumped.project.calm

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController

@Composable
fun FluidSimulationScreen(navController: NavController) {

    val context = LocalContext.current
    val fluidRenderer = remember { FluidRenderer(context) }

    AndroidView(
        factory = {
            GLSurfaceView(it).apply {
                // Use OpenGL ES 3.0
                setEGLContextClientVersion(3)

                // Request a standard 8-bit RGBA surface. This is a key fix.
                setEGLConfigChooser(8, 8, 8, 8, 16, 0)

                setRenderer(fluidRenderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

                setOnTouchListener { _, event ->
                    fluidRenderer.handleTouchEvent(event)
                    true
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}