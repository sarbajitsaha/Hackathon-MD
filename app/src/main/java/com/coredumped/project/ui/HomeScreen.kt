package com.coredumped.project.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavController
import com.coredumped.project.R

@Composable
fun HomeScreen(navController: NavController) {
    // Container Box to hold the background and grid
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image that fills the entire screen
        Image(
            painter = painterResource(id = R.drawable.homescreen1),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Crop to maintain aspect ratio and fill screen
        )

        // Grid layout for category items
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // Fixed 2-column layout
            modifier = Modifier
                .fillMaxSize() // Fill the available space
                .padding(8.dp), // Outer padding around the grid
            verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically), // Vertical spacing between items
            horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterHorizontally), // Horizontal spacing between items
            contentPadding = PaddingValues(0.dp) // Padding inside the grid content
        ) {
            item {
                CategoryItem(
                    text = "Daily Activity",
                    imageResId = R.drawable.brush,
                    color = Color(0xFF4CAF50),
                    onClick = { navController.navigate("daily_activity") }
                )
            }
            item {
                CategoryItem(
                    text = "Learning",
                    imageResId = R.drawable.learning1,
                    color = Color(0xFFFFC107),
                    onClick = { navController.navigate("learning") }
                )
            }
            item {
                CategoryItem(
                    text = "IQ",
                    imageResId = R.drawable.iq1,
                    color = Color(0xFF2196F3),
                    onClick = { navController.navigate("iq") }
                )
            }
            item {
                CategoryItem(
                    text = "Calm",
                    imageResId = R.drawable.calm1,
                    color = Color(0xFF9C27B0),
                    onClick = { navController.navigate("calm") }
                )
            }
        }

        val context = LocalContext.current
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
                    context.findActivity()?.finish()
                }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}


// Utility function to get Activity from Context (non-composable)
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun CategoryItem(
    text: String, // Keeping param for consistency, not used in display
    imageResId: Int,
    color: Color,
    onClick: () -> Unit
) {
    // Box to center the card within the grid cell
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Center the card horizontally and vertically
    ) {
        // Card to hold the image with a translucent background
        Card(
            modifier = Modifier
                .clickable(onClick = onClick) // Make the card clickable
                .size(150.dp), // Fixed size to match image, no extra padding
            shape = RoundedCornerShape(12.dp), // Rounded corners for the card
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)), // Subtle translucent background
            elevation = CardDefaults.cardElevation(1.dp) // Slight elevation for depth
        ) {
            // Image filling the card
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = text,
                modifier = Modifier
                    .fillMaxSize() // Fill the card entirely
                    .clip(RoundedCornerShape(8.dp)), // Rounded corners for the image
                contentScale = ContentScale.Crop // Crop to fill while maintaining aspect ratio
            )
        }
    }
}