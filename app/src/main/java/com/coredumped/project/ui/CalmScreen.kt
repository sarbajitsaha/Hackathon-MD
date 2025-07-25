package com.coredumped.project.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coredumped.project.R

@Composable
fun CalmScreen(navController: NavController) {
    // Container Box to hold the background and grid
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image that fills the entire screen
        Image(
            painter = painterResource(id = R.drawable.homescreen),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Crop to maintain aspect ratio and fill screen
        )

        // Grid layout for category items
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Fixed 2-column layout
            modifier = Modifier
                .fillMaxSize() // Fill the available space
                .padding(8.dp), // Outer padding around the grid
            verticalArrangement = Arrangement.spacedBy(12.dp), // Vertical spacing between items
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Horizontal spacing between items
            contentPadding = PaddingValues(4.dp) // Padding inside the grid content
        ) {
            item {
                CategoryItem(
                    text = "Fluid",
                    imageResId = R.drawable.fluid,
                    color = Color.Blue,
                    onClick = { navController.navigate("fluid_simulation") }
                )
            }
            item {
                CategoryItem(
                    text = "Audio",
                    imageResId = R.drawable.calm_audio,
                    color = Color.Green,
                    onClick = { navController.navigate("calming_audio") }
                )
            }
            item {
                CategoryItem(
                    text = "Video",
                    imageResId = R.drawable.calm_video,
                    color = Color.Blue,
                    onClick = { navController.navigate("calming_video") }
                )
            }
            item {
                CategoryItem(
                    text = "Bubbles",
                    imageResId = R.drawable.bubbles,
                    color = Color.Cyan,
                    onClick = { navController.navigate("pop_bubble") }
                )
            }
        }
    }
}

/*
Commenting this out for now, we should just use the homescreen one here
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
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.3f)), // Subtle translucent background
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
 */