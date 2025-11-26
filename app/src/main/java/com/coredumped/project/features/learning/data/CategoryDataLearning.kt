package com.coredumped.project.features.learning.data

import androidx.compose.ui.graphics.Color

// Data class for category information
data class CategoryDataLearning(
    val text: String,
    val imageResId: Int,
    val color: Color, // This color is not used in CategoryItemLearning currently
    val route: String
)

