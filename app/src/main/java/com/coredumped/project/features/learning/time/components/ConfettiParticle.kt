package com.coredumped.project.features.learning.time.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

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
